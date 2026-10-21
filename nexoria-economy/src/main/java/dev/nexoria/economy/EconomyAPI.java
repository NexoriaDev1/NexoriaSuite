package dev.nexoria.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
    private DecimalFormat formatter = new DecimalFormat("#,##0.00");

    public EconomyAPI(NexoriaEconomyPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dataFile = new File(plugin.getDataFolder(), "data/balances.yml");
        this.active = true;
        load();
    }

    public void load() {
        var cfg = plugin.getConfig();
        this.symbol = cfg.getString("currency.symbol", "$");
        this.nameSingular = cfg.getString("currency.name-singular", "Dollar");
        this.namePlural = cfg.getString("currency.name-plural", "Dollars");
        this.startingBalance = cfg.getDouble("currency.starting-balance", 1000.0);
        int dec = cfg.getInt("currency.decimals", 2);
        String pattern = dec > 0 ? "#,##0." + "0".repeat(dec) : "#,##0";
        this.formatter = new DecimalFormat(pattern);

        balances.clear();
        knownNames.clear();

        if (dataFile.exists()) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
            for (String key : yaml.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    double val = yaml.getDouble(key + ".balance", startingBalance);
                    String name = yaml.getString(key + ".name", "Unknown");
                    balances.put(uuid, val);
                    knownNames.put(uuid, name);
                } catch (IllegalArgumentException ignored) {
                    // Legacy flat format fallback
                    try {
                        UUID uuid = UUID.fromString(key);
                        balances.put(uuid, yaml.getDouble(key));
                    } catch (Exception ignored2) {}
                }
            }
        }
    }

    public synchronized void save() {
        try {
            if (!dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }
            YamlConfiguration yaml = new YamlConfiguration();
            balances.forEach((uuid, amount) -> {
                String name = knownNames.getOrDefault(uuid, "Unknown");
                yaml.set(uuid + ".name", name);
                yaml.set(uuid + ".balance", amount);
            });
            yaml.save(dataFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save balance data: " + e.getMessage());
        }
    }

    public void saveAsync() {
        CompletableFuture.runAsync(this::save);
    }

    public boolean isActive() {
        return active;
    }

    public double getBalance(UUID uuid) {
        return balances.computeIfAbsent(uuid, k -> startingBalance);
    }

    public boolean setBalance(UUID uuid, double amount) {
        if (amount < 0) return false;
        balances.put(uuid, amount);
        saveAsync();
        return true;
    }

    public boolean deposit(UUID uuid, double amount) {
        if (amount <= 0) return false;
        balances.put(uuid, getBalance(uuid) + amount);
        saveAsync();
        return true;
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (amount <= 0) return false;
        double current = getBalance(uuid);
        if (current < amount) return false;
        balances.put(uuid, current - amount);
        saveAsync();
        return true;
    }

    public boolean has(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    public String format(double amount) {
        return symbol + formatter.format(amount);
    }

    public void updatePlayerName(UUID uuid, String name) {
        knownNames.put(uuid, name);
    }

    public String getPlayerName(UUID uuid) {
        String cached = knownNames.get(uuid);
        if (cached != null) return cached;
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        return op.getName() != null ? op.getName() : "Unknown";
    }

    public List<AccountEntry> getTopBalances(int limit) {
        List<AccountEntry> list = new ArrayList<>();
        balances.forEach((uuid, bal) -> {
            list.add(new AccountEntry(uuid, getPlayerName(uuid), bal));
        });
        list.sort((a, b) -> Double.compare(b.balance(), a.balance()));
        if (list.size() > limit) {
            return list.subList(0, limit);
        }
        return list;
    }

    public double getTotalCirculation() {
        return balances.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public int getTotalAccounts() {
        return balances.size();
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
    }
}
