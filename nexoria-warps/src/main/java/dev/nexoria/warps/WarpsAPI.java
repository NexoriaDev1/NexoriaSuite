package dev.nexoria.warps;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Thread-safe Warps API supporting Folia and Paper.
 * Handles storage, coordinate serialization, and warp validation.
 */
public class WarpsAPI {

    public record ServiceHealth(String service, boolean active, int totalWarps, Instant timestamp) {}

    public record WarpData(String name, String worldName, double x, double y, double z, float yaw, float pitch) {
        public Location toLocation() {
            World world = Bukkit.getWorld(worldName);
            if (world == null) return null;
            return new Location(world, x, y, z, yaw, pitch);
        }
    }

    private final NexoriaWarpsPlugin plugin;
    private final Map<String, WarpData> warpRegistry = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final File storageFile;
    private volatile boolean active;
    private double cooldownSeconds;

    public WarpsAPI(NexoriaWarpsPlugin plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "data/warps.yml");
        this.active = true;
        reload();
    }

    public void reload() {
        this.cooldownSeconds = plugin.getConfig().getDouble("warps.cooldown-seconds", 5.0);
        loadWarps();
    }

    public boolean isActive() {
        return active;
    }

    public ServiceHealth getStatus() {
        return new ServiceHealth("NexoriaWarps", active, warpRegistry.size(), Instant.now());
    }

    public synchronized void loadWarps() {
        warpRegistry.clear();
        if (!storageFile.exists()) return;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(storageFile);
        for (String key : yaml.getKeys(false)) {
            String world = yaml.getString(key + ".world");
            double x = yaml.getDouble(key + ".x");
            double y = yaml.getDouble(key + ".y");
            double z = yaml.getDouble(key + ".z");
            float yaw = (float) yaml.getDouble(key + ".yaw");
            float pitch = (float) yaml.getDouble(key + ".pitch");

            if (world != null) {
                warpRegistry.put(key.toLowerCase(Locale.ROOT), new WarpData(key, world, x, y, z, yaw, pitch));
            }
        }
    }

    public synchronized void saveWarps() {
        try {
            if (!storageFile.getParentFile().exists()) {
                storageFile.getParentFile().mkdirs();
            }
            YamlConfiguration yaml = new YamlConfiguration();
            for (Map.Entry<String, WarpData> entry : warpRegistry.entrySet()) {
                WarpData w = entry.getValue();
                yaml.set(w.name() + ".world", w.worldName());
                yaml.set(w.name() + ".x", w.x());
                yaml.set(w.name() + ".y", w.y());
                yaml.set(w.name() + ".z", w.z());
                yaml.set(w.name() + ".yaw", w.yaw());
                yaml.set(w.name() + ".pitch", w.pitch());
            }
            yaml.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save warps to file: " + e.getMessage());
        }
    }

    public void setWarp(String name, Location loc) {
        String key = name.toLowerCase(Locale.ROOT);
        WarpData data = new WarpData(name, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        warpRegistry.put(key, data);
        CompletableFuture.runAsync(this::saveWarps);
    }

    public boolean deleteWarp(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        WarpData removed = warpRegistry.remove(key);
        if (removed != null) {
            CompletableFuture.runAsync(this::saveWarps);
            return true;
        }
        return false;
    }

    public WarpData getWarp(String name) {
        return warpRegistry.get(name.toLowerCase(Locale.ROOT));
    }

    public Set<String> getWarpNames() {
        return Collections.unmodifiableSet(warpRegistry.keySet());
    }

    public boolean checkCooldown(UUID uuid) {
        if (cooldownSeconds <= 0) return true;
        Long last = cooldowns.get(uuid);
        long now = System.currentTimeMillis();
        if (last != null) {
            long diff = now - last;
            if (diff < (long)(cooldownSeconds * 1000)) {
                return false;
            }
        }
        cooldowns.put(uuid, now);
        return true;
    }

    public double getRemainingCooldown(UUID uuid) {
        Long last = cooldowns.get(uuid);
        if (last == null) return 0.0;
        long diff = System.currentTimeMillis() - last;
        long required = (long)(cooldownSeconds * 1000);
        if (diff >= required) return 0.0;
        return ((required - diff) / 100.0) / 10.0;
    }

    public void invalidateSession(UUID uuid) {
        cooldowns.remove(uuid);
    }

    protected void shutdown() {
        this.active = false;
        saveWarps();
        warpRegistry.clear();
        cooldowns.clear();
    }
}
