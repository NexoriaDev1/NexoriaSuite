package dev.nexoria.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EconomyAPI {

    public record AccountEntry(UUID uuid, String name, double balance) {}

    private final NexoriaEconomyPlugin plugin;
    private final Logger logger;
    private final Map<UUID, Double> balances = new ConcurrentHashMap<>();
    private final Map<UUID, String> knownNames = new ConcurrentHashMap<>();
    private final File dataFile;
    private volatile boolean active;

    private String symbol = "$";
    private String nameSingular = "Dollar";
    private String namePlural = "Dollars";
    private double startingBalance = 1000.0;
    private double minPayAmount = 1.0;
    private int decimals = 2;
    private DecimalFormat formatter;

    // Baltop caching
    private List<AccountEntry> cachedBaltop = Collections.emptyList();
    private long lastBaltopUpdate = 0;
    private long baltopCacheDurationMs = 60_000;

    // Debounced autosave
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    public EconomyAPI(NexoriaEconomyPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dataFile = new File(plugin.getDataFolder(), "data/balances.yml");
        this.active = true;
        load();
        startAutoSaver();
    }

    public void load() {
        var cfg = plugin.getConfig();
        this.symbol = cfg.getString("currency.symbol", "$");
        this.nameSingular = cfg.getString("currency.name-singular", "Dollar");
        this.namePlural = cfg.getString("currency.name-plural", "Dollars");
        this.startingBalance = cfg.getDouble("currency.starting-balance", 1000.0);
        this.minPayAmount = cfg.getDouble("currency.min-pay-amount", 1.0);
        this.decimals = Math.max(0, cfg.getInt("currency.decimals", 2));
        this.baltopCacheDurationMs = Math.max(10, cfg.getInt("baltop.cache-minutes", 5)) * 60_000L;

        String pattern = decimals > 0 ? "#,##0." + "0".repeat(decimals) : "#,##0";
        this.formatter = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.US));

        balances.clear();
        knownNames.clear();
        invalidateBaltopCache();

        if (dataFile.exists()) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
            for (String key : yaml.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    double val = yaml.getDouble(key + ".balance", startingBalance);
                    String name = yaml.getString(key + ".name", "Unknown");
                    balances.put(uuid, round(val));
                    knownNames.put(uuid, name);
                } catch (IllegalArgumentException ignored) {
                    try {
                        UUID uuid = UUID.fromString(key);
                        balances.put(uuid, round(yaml.getDouble(key)));
                    } catch (Exception ignored2) {}
                }
            }
        }
    }

    private void startAutoSaver() {
        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> {
            if (!active) {
                task.cancel();
                return;
            }
            if (dirty.compareAndSet(true, false)) {
                save();
            }
        }, 30, 30, java.util.concurrent.TimeUnit.SECONDS);
    }

    public synchronized void save() {
        if (!dataFile.getParentFile().exists()) {
            dataFile.getParentFile().mkdirs();
        }

        File tempFile = new File(dataFile.getParentFile(), "balances.yml.tmp");
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            balances.forEach((uuid, amount) -> {
                String name = knownNames.getOrDefault(uuid, "Unknown");
                yaml.set(uuid + ".name", name);
                yaml.set(uuid + ".balance", amount);
            });
            yaml.save(tempFile);
            Files.move(tempFile.toPath(), dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save balance data: " + e.getMessage(), e);
        }
    }

    public void markDirty() {
        dirty.set(true);
        invalidateBaltopCache();
    }

    public boolean isActive() {
        return active;
    }

    public boolean hasAccount(UUID uuid) {
        if (uuid == null) return false;
        if (balances.containsKey(uuid)) return true;
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        return op.hasPlayedBefore() || op.isOnline();
    }

    public boolean createAccount(UUID uuid, String name) {
        if (uuid == null) return false;
        if (balances.containsKey(uuid)) return false;
        balances.put(uuid, round(startingBalance));
        if (name != null && !name.isEmpty()) {
            knownNames.put(uuid, name);
        }
        markDirty();
        return true;
    }

    public double getBalance(UUID uuid) {
        if (uuid == null) return 0.0;
        Double bal = balances.get(uuid);
        if (bal != null) return bal;

        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        if (op.hasPlayedBefore() || op.isOnline()) {
            balances.put(uuid, startingBalance);
            if (op.getName() != null) {
                knownNames.put(uuid, op.getName());
            }
            markDirty();
            return startingBalance;
        }
        return 0.0;
    }

    public boolean setBalance(UUID uuid, double amount) {
        if (uuid == null || amount < 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
            return false;
        }
        balances.put(uuid, round(amount));
        markDirty();
        return true;
    }

    public boolean resetBalance(UUID uuid) {
        if (uuid == null) return false;
        balances.put(uuid, round(startingBalance));
        markDirty();
        return true;
    }

    public boolean deposit(UUID uuid, double amount) {
        if (uuid == null || amount < 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
            return false;
        }
        if (amount == 0.0) return true;
        double current = getBalance(uuid);
        balances.put(uuid, round(current + amount));
        markDirty();
        return true;
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (uuid == null || amount < 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
            return false;
        }
        if (amount == 0.0) return true;
        double current = getBalance(uuid);
        if (current < amount) return false;
        balances.put(uuid, round(current - amount));
        markDirty();
        return true;
    }

    public boolean has(UUID uuid, double amount) {
        if (uuid == null || amount < 0) return false;
        return getBalance(uuid) >= amount;
    }

    public double round(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return 0.0;
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public String format(double amount) {
        return symbol + formatter.format(round(amount));
    }

    public void updatePlayerName(UUID uuid, String name) {
        if (uuid != null && name != null && !name.isEmpty()) {
            knownNames.put(uuid, name);
            if (!balances.containsKey(uuid)) {
                balances.put(uuid, startingBalance);
                markDirty();
            }
        }
    }

    public String getPlayerName(UUID uuid) {
        String cached = knownNames.get(uuid);
        if (cached != null) return cached;
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        return op.getName() != null ? op.getName() : "Unknown";
    }

    public synchronized void invalidateBaltopCache() {
        lastBaltopUpdate = 0;
    }

    public synchronized List<AccountEntry> getSortedAccounts() {
        long now = System.currentTimeMillis();
        if (cachedBaltop.isEmpty() || (now - lastBaltopUpdate) > baltopCacheDurationMs) {
            List<AccountEntry> list = new ArrayList<>(balances.size());
            balances.forEach((uuid, bal) -> {
                list.add(new AccountEntry(uuid, getPlayerName(uuid), bal));
            });
            list.sort((a, b) -> Double.compare(b.balance(), a.balance()));
            cachedBaltop = Collections.unmodifiableList(list);
            lastBaltopUpdate = now;
        }
        return cachedBaltop;
    }

    public List<AccountEntry> getTopBalances(int page, int perPage) {
        List<AccountEntry> all = getSortedAccounts();
        if (all.isEmpty()) return Collections.emptyList();

        int fromIndex = (page - 1) * perPage;
        if (fromIndex >= all.size()) return Collections.emptyList();
        int toIndex = Math.min(fromIndex + perPage, all.size());
        return all.subList(fromIndex, toIndex);
    }

    public int getMaxBaltopPages(int perPage) {
        int total = balances.size();
        if (total == 0) return 1;
        return (int) Math.ceil((double) total / perPage);
    }

    public double getTotalCirculation() {
        return balances.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public int getTotalAccounts() {
        return balances.size();
    }

    public double getStartingBalance() {
        return startingBalance;
    }

    public double getMinPayAmount() {
        return minPayAmount;
    }

    public int getDecimals() {
        return decimals;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getNameSingular() {
        return nameSingular;
    }

    public String getNamePlural() {
        return namePlural;
    }

    public void shutdown() {
        this.active = false;
        save();
        balances.clear();
        knownNames.clear();
        cachedBaltop = Collections.emptyList();
    }
}
