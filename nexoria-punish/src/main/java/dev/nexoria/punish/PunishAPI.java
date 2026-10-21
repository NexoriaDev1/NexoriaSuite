package dev.nexoria.punish;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Thread-safe Punishment API managing bans and mutes with file persistence.
 */
public class PunishAPI {

    public record ServiceHealth(String service, boolean active, int totalBans, int totalMutes, Instant timestamp) {}

    public record Punishment(UUID targetUuid, String targetName, String staff, String reason, long timestamp) {}

    private final NexoriaPunishPlugin plugin;
    private final Map<UUID, Punishment> bans = new ConcurrentHashMap<>();
    private final Map<UUID, Punishment> mutes = new ConcurrentHashMap<>();
    private final File dataFile;
    private volatile boolean active;

    public PunishAPI(NexoriaPunishPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data/punishments.yml");
        this.active = true;
        loadData();
    }

    public boolean isActive() {
        return active;
    }

    public ServiceHealth getStatus() {
        return new ServiceHealth("NexoriaPunish", active, bans.size(), mutes.size(), Instant.now());
    }

    public synchronized void loadData() {
        bans.clear();
        mutes.clear();
        if (!dataFile.exists()) return;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        if (yaml.isConfigurationSection("bans")) {
            for (String key : yaml.getConfigurationSection("bans").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String target = yaml.getString("bans." + key + ".target");
                    String staff = yaml.getString("bans." + key + ".staff");
                    String reason = yaml.getString("bans." + key + ".reason");
                    long time = yaml.getLong("bans." + key + ".timestamp");
                    bans.put(uuid, new Punishment(uuid, target, staff, reason, time));
                } catch (Exception ignored) {}
            }
        }

        if (yaml.isConfigurationSection("mutes")) {
            for (String key : yaml.getConfigurationSection("mutes").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String target = yaml.getString("mutes." + key + ".target");
                    String staff = yaml.getString("mutes." + key + ".staff");
                    String reason = yaml.getString("mutes." + key + ".reason");
                    long time = yaml.getLong("mutes." + key + ".timestamp");
                    mutes.put(uuid, new Punishment(uuid, target, staff, reason, time));
                } catch (Exception ignored) {}
            }
        }
    }

    public synchronized void saveData() {
        try {
            if (!dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }
            YamlConfiguration yaml = new YamlConfiguration();
            bans.forEach((uuid, p) -> {
                yaml.set("bans." + uuid + ".target", p.targetName());
                yaml.set("bans." + uuid + ".staff", p.staff());
                yaml.set("bans." + uuid + ".reason", p.reason());
                yaml.set("bans." + uuid + ".timestamp", p.timestamp());
            });
            mutes.forEach((uuid, p) -> {
                yaml.set("mutes." + uuid + ".target", p.targetName());
                yaml.set("mutes." + uuid + ".staff", p.staff());
                yaml.set("mutes." + uuid + ".reason", p.reason());
                yaml.set("mutes." + uuid + ".timestamp", p.timestamp());
            });
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save punishments to disk: " + e.getMessage());
        }
    }

    public void ban(UUID target, String targetName, String staff, String reason) {
        bans.put(target, new Punishment(target, targetName, staff, reason, System.currentTimeMillis()));
        CompletableFuture.runAsync(this::saveData);
    }

    public boolean unban(UUID target) {
        Punishment removed = bans.remove(target);
        if (removed != null) {
            CompletableFuture.runAsync(this::saveData);
            return true;
        }
        return false;
    }

    public boolean isBanned(UUID target) {
        return bans.containsKey(target);
    }

    public Punishment getBan(UUID target) {
        return bans.get(target);
    }

    public void mute(UUID target, String targetName, String staff, String reason) {
        mutes.put(target, new Punishment(target, targetName, staff, reason, System.currentTimeMillis()));
        CompletableFuture.runAsync(this::saveData);
    }

    public boolean unmute(UUID target) {
        Punishment removed = mutes.remove(target);
        if (removed != null) {
            CompletableFuture.runAsync(this::saveData);
            return true;
        }
        return false;
    }

    public boolean isMuted(UUID target) {
        return mutes.containsKey(target);
    }

    public Punishment getMute(UUID target) {
        return mutes.get(target);
    }

    protected void shutdown() {
        this.active = false;
        saveData();
        bans.clear();
        mutes.clear();
    }
}
