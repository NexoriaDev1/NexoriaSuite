package dev.nexoria.loot;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class LootTableManager {

    private final NexoriaLootPlugin plugin;
    private final Map<String, List<LootEntry>> entityDrops = new ConcurrentHashMap<>();
    private final Map<String, List<LootEntry>> blockDrops = new ConcurrentHashMap<>();

    public LootTableManager(NexoriaLootPlugin plugin) {
        this.plugin = plugin;
        loadLoot();
    }

    public synchronized void loadLoot() {
        entityDrops.clear();
        blockDrops.clear();

        File file = new File(plugin.getDataFolder(), "loot.yml");
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try (InputStream in = plugin.getResource("loot.yml")) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to copy loot.yml", e);
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection mobSec = config.getConfigurationSection("mobs");
        if (mobSec != null) {
            for (String mobName : mobSec.getKeys(false)) {
                List<?> rawList = mobSec.getList(mobName);
                if (rawList == null) continue;
                List<LootEntry> entries = new ArrayList<>();
                for (Object obj : rawList) {
                    if (!(obj instanceof Map<?, ?> itemMap)) continue;
                    try {
                        Object itemObj = itemMap.get("item");
                        if (itemObj == null) continue;
                        Material mat = Material.matchMaterial(itemObj.toString().toUpperCase());
                        if (mat == null) continue;

                        Object cObj = itemMap.get("chance");
                        double chance = cObj != null ? Double.parseDouble(cObj.toString()) : 10.0;

                        Object minObj = itemMap.get("min");
                        int min = minObj != null ? Integer.parseInt(minObj.toString()) : 1;

                        Object maxObj = itemMap.get("max");
                        int max = maxObj != null ? Integer.parseInt(maxObj.toString()) : 1;

                        entries.add(new LootEntry(mobName, mat, chance, min, max));
                    } catch (Exception ignored) {}
                }
                if (!entries.isEmpty()) {
                    entityDrops.put(mobName.toUpperCase(), entries);
                }
            }
        }

        ConfigurationSection blockSec = config.getConfigurationSection("blocks");
        if (blockSec != null) {
            for (String blockName : blockSec.getKeys(false)) {
                List<?> rawList = blockSec.getList(blockName);
                if (rawList == null) continue;
                List<LootEntry> entries = new ArrayList<>();
                for (Object obj : rawList) {
                    if (!(obj instanceof Map<?, ?> itemMap)) continue;
                    try {
                        Object itemObj = itemMap.get("item");
                        if (itemObj == null) continue;
                        Material mat = Material.matchMaterial(itemObj.toString().toUpperCase());
                        if (mat == null) continue;

                        Object cObj = itemMap.get("chance");
                        double chance = cObj != null ? Double.parseDouble(cObj.toString()) : 10.0;

                        Object minObj = itemMap.get("min");
                        int min = minObj != null ? Integer.parseInt(minObj.toString()) : 1;

                        Object maxObj = itemMap.get("max");
                        int max = maxObj != null ? Integer.parseInt(maxObj.toString()) : 1;

                        entries.add(new LootEntry(blockName, mat, chance, min, max));
                    } catch (Exception ignored) {}
                }
                if (!entries.isEmpty()) {
                    blockDrops.put(blockName.toUpperCase(), entries);
                }
            }
        }

        plugin.getLogger().info("Loaded loot tables: " + entityDrops.size() + " mobs, " + blockDrops.size() + " blocks.");
    }

    public List<LootEntry> getEntityDrops(String entityType) {
        return entityDrops.getOrDefault(entityType.toUpperCase(), Collections.emptyList());
    }

    public List<LootEntry> getBlockDrops(String blockType) {
        return blockDrops.getOrDefault(blockType.toUpperCase(), Collections.emptyList());
    }

    public Map<String, List<LootEntry>> getAllEntityDrops() {
        return Collections.unmodifiableMap(entityDrops);
    }

    public Map<String, List<LootEntry>> getAllBlockDrops() {
        return Collections.unmodifiableMap(blockDrops);
    }
}
