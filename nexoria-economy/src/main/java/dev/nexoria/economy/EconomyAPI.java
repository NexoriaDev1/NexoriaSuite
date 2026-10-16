package dev.nexoria.economy;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Developer API contract for NexoriaEconomy.
 * High-performance, thread-safe economy engine for Paper and Folia.
 */
public class EconomyAPI {

    public record ServiceHealth(String service, boolean active, Instant timestamp, int totalAccounts, double totalCirculation) {}

    private final NexoriaEconomyPlugin plugin;
    private final Logger logger;
    private final Map<UUID, Double> balanceRegistry = new ConcurrentHashMap<>();
    private final DecimalFormat formatter = new DecimalFormat("#,##0.00");
    private final File dataFile;
    private volatile boolean active;
    private String currencySymbol = "$";
    private double startingBalance = 1000.0;

    public EconomyAPI(NexoriaEconomyPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.active = true;
        this.dataFile = new File(plugin.getDataFolder(), "data/balances.yml");
        loadData();
    }

    public void loadData() {
        this.currencySymbol = plugin.getConfig().getString("currency.symbol", "$");
        this.startingBalance = plugin.getConfig().getDouble("currency.starting-balance", 1000.0);

        if (dataFile.exists()) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
            for (String key : yaml.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    balanceRegistry.put(uuid, yaml.getDouble(key));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public synchronized void saveData() {
        try {
            if (!dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }
            YamlConfiguration yaml = new YamlConfiguration();
            balanceRegistry.forEach((uuid, bal) -> yaml.set(uuid.toString(), bal));
            yaml.save(dataFile);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to persist balance data: {0}", e.getMessage());
        }
    }

    public boolean isActive() {
        return active;
    }

    public ServiceHealth getStatus() {
        double total = balanceRegistry.values().stream().mapToDouble(Double::doubleValue).sum();
        return new ServiceHealth("NexoriaEconomy", active, Instant.now(), balanceRegistry.size(), total);
    }

    public double getBalance(UUID uuid) {
        return balanceRegistry.computeIfAbsent(uuid, k -> startingBalance);
    }

    public boolean setBalance(UUID uuid, double amount) {
        if (amount < 0) return false;
        balanceRegistry.put(uuid, amount);
        saveAsync();
        return true;
    }

    public boolean deposit(UUID uuid, double amount) {
        if (amount <= 0) return false;
        double current = getBalance(uuid);
        balanceRegistry.put(uuid, current + amount);
        saveAsync();
        return true;
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (amount <= 0) return false;
        double current = getBalance(uuid);
        if (current < amount) return false;
        balanceRegistry.put(uuid, current - amount);
        saveAsync();
        return true;
    }

    public boolean has(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    public String format(double amount) {
        return currencySymbol + formatter.format(amount);
    }

    public void saveAsync() {
        CompletableFuture.runAsync(this::saveData);
    }

    public CompletableFuture<Void> supplyAsync(Runnable task) {
        return CompletableFuture.runAsync(task);
    }

    public Map<UUID, Double> getActiveSessions() {
        return Collections.unmodifiableMap(balanceRegistry);
    }

    public void registerSession(UUID uuid) {
        getBalance(uuid); // Ensure account is initialized
    }

    public void invalidateSession(UUID uuid) {
        // Balances remain persisted in registry and disk
    }

    protected void shutdown() {
        this.active = false;
        saveData();
        this.balanceRegistry.clear();
    }

    public NexoriaEconomyPlugin getPlugin() {
        return plugin;
    }
}
