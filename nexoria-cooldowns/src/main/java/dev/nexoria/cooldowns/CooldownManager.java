package dev.nexoria.cooldowns;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private final NexoriaCooldownsPlugin plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();
    private final File dataFile;

    public CooldownManager(NexoriaCooldownsPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data/cooldowns.yml");
        loadData();
    }

    public boolean isOnCooldown(UUID uuid, String key) {
        Map<String, Long> map = cooldowns.get(uuid);
        if (map == null) return false;
        Long expire = map.get(key.toLowerCase());
        if (expire == null) return false;
        if (System.currentTimeMillis() >= expire) {
            map.remove(key.toLowerCase());
            return false;
        }
        return true;
    }

    public long getRemainingMillis(UUID uuid, String key) {
        Map<String, Long> map = cooldowns.get(uuid);
        if (map == null) return 0;
        Long expire = map.get(key.toLowerCase());
        if (expire == null) return 0;
        long diff = expire - System.currentTimeMillis();
        return Math.max(0, diff);
    }

    public void setCooldown(UUID uuid, String key, long seconds) {
        cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(key.toLowerCase(), System.currentTimeMillis() + (seconds * 1000L));
    }

    public void resetCooldown(UUID uuid, String key) {
        Map<String, Long> map = cooldowns.get(uuid);
        if (map != null) {
            map.remove(key.toLowerCase());
        }
    }

    public void resetAll(UUID uuid) {
        cooldowns.remove(uuid);
    }

    public Map<String, Long> getActiveCooldowns(UUID uuid) {
        Map<String, Long> map = cooldowns.get(uuid);
        if (map == null) return Collections.emptyMap();

        long now = System.currentTimeMillis();
        Map<String, Long> active = new HashMap<>();
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            if (entry.getValue() > now) {
                active.put(entry.getKey(), entry.getValue() - now);
            }
        }
        return active;
    }

    public String formatTime(long millis) {
        long sec = millis / 1000L;
        if (sec < 60) return sec + "s";
        long min = sec / 60;
        long remSec = sec % 60;
        if (min < 60) return min + "m " + remSec + "s";
        long hours = min / 60;
        long remMin = min % 60;
        return hours + "h " + remMin + "m";
    }

    public void loadData() {
        if (!dataFile.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection root = yaml.getConfigurationSection("cooldowns");
        if (root == null) return;

        long now = System.currentTimeMillis();
        for (String uuidStr : root.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection sec = root.getConfigurationSection(uuidStr);
                if (sec != null) {
                    Map<String, Long> map = new ConcurrentHashMap<>();
                    for (String key : sec.getKeys(false)) {
                        long expire = sec.getLong(key);
                        if (expire > now) {
                            map.put(key.toLowerCase(), expire);
                        }
                    }
                    if (!map.isEmpty()) {
                        cooldowns.put(uuid, map);
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public void saveData() {
        YamlConfiguration yaml = new YamlConfiguration();
        long now = System.currentTimeMillis();

        for (Map.Entry<UUID, Map<String, Long>> entry : cooldowns.entrySet()) {
            for (Map.Entry<String, Long> sub : entry.getValue().entrySet()) {
                if (sub.getValue() > now) {
                    yaml.set("cooldowns." + entry.getKey().toString() + "." + sub.getKey(), sub.getValue());
                }
            }
        }

        try {
            if (!dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save cooldowns.yml: " + e.getMessage());
        }
    }
}
