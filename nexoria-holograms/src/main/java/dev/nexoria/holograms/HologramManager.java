package dev.nexoria.holograms;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HologramManager {

    private final NexoriaHologramsPlugin plugin;
    private final Map<String, Hologram> holograms = new ConcurrentHashMap<>();
    private final File dataFile;

    public HologramManager(NexoriaHologramsPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data/holograms.yml");
        loadData();
    }

    public boolean createHologram(String name, Location loc) {
        if (holograms.containsKey(name.toLowerCase())) {
            return false;
        }

        Hologram holo = new Hologram(name.toLowerCase(), loc);
        holo.addLine("&eHologram &6" + name);
        holo.spawn(getSpacing(), getBillboard());
        holograms.put(name.toLowerCase(), holo);

        saveData();
        return true;
    }

    public boolean deleteHologram(String name) {
        Hologram holo = holograms.remove(name.toLowerCase());
        if (holo != null) {
            holo.despawn();
            saveData();
            return true;
        }
        return false;
    }

    public boolean addLine(String name, String text) {
        Hologram holo = holograms.get(name.toLowerCase());
        if (holo != null) {
            holo.addLine(text);
            holo.spawn(getSpacing(), getBillboard());
            saveData();
            return true;
        }
        return false;
    }

    public boolean setLine(String name, int index, String text) {
        Hologram holo = holograms.get(name.toLowerCase());
        if (holo != null) {
            holo.setLine(index, text);
            holo.spawn(getSpacing(), getBillboard());
            saveData();
            return true;
        }
        return false;
    }

    public boolean moveHologram(String name, Location loc) {
        Hologram holo = holograms.get(name.toLowerCase());
        if (holo != null) {
            holo.setLocation(loc);
            holo.spawn(getSpacing(), getBillboard());
            saveData();
            return true;
        }
        return false;
    }

    public Hologram getHologram(String name) {
        return holograms.get(name.toLowerCase());
    }

    public Map<String, Hologram> getHolograms() {
        return Collections.unmodifiableMap(holograms);
    }

    private double getSpacing() {
        return plugin.getConfig().getDouble("settings.line-spacing", 0.28);
    }

    private Display.Billboard getBillboard() {
        String mode = plugin.getConfig().getString("settings.billboard", "CENTER");
        try {
            return Display.Billboard.valueOf(mode.toUpperCase());
        } catch (Exception e) {
            return Display.Billboard.CENTER;
        }
    }

    public void reload() {
        despawnAll();
        holograms.clear();
        loadData();
    }

    public void despawnAll() {
        for (Hologram h : holograms.values()) {
            h.despawn();
        }
    }

    public void loadData() {
        if (!dataFile.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection root = yaml.getConfigurationSection("holograms");
        if (root == null) return;

        for (String key : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(key);
            if (sec == null) continue;

            String worldName = sec.getString("world");
            if (worldName == null) continue;
            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;

            double x = sec.getDouble("x");
            double y = sec.getDouble("y");
            double z = sec.getDouble("z");
            Location loc = new Location(world, x, y, z);

            Hologram holo = new Hologram(key.toLowerCase(), loc);
            List<String> lines = sec.getStringList("lines");
            for (String l : lines) {
                holo.addLine(l);
            }

            holo.spawn(getSpacing(), getBillboard());
            holograms.put(key.toLowerCase(), holo);
        }
    }

    public void saveData() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<String, Hologram> entry : holograms.entrySet()) {
            Hologram h = entry.getValue();
            String path = "holograms." + entry.getKey() + ".";
            if (h.getLocation() != null && h.getLocation().getWorld() != null) {
                yaml.set(path + "world", h.getLocation().getWorld().getName());
                yaml.set(path + "x", h.getLocation().getX());
                yaml.set(path + "y", h.getLocation().getY());
                yaml.set(path + "z", h.getLocation().getZ());
                yaml.set(path + "lines", h.getLines());
            }
        }

        try {
            if (!dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save holograms.yml: " + e.getMessage());
        }
    }
}
