package dev.nexoria.sync;

import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class SyncManager {

    private final NexoriaSyncPlugin plugin;
    private final Map<UUID, SyncData> syncCache = new ConcurrentHashMap<>();
    private final File dataFile;

    public SyncManager(NexoriaSyncPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "sync_storage.yml");
        loadData();
    }

    public synchronized void loadData() {
        syncCache.clear();
        if (!dataFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection root = config.getConfigurationSection("data");
        if (root == null) return;

        for (String uuidStr : root.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection sec = root.getConfigurationSection(uuidStr);
                if (sec == null) continue;

                String inv = sec.getString("inventory", "");
                String ec = sec.getString("enderchest", "");
                double hp = sec.getDouble("health", 20.0);
                int food = sec.getInt("food", 20);
                float sat = (float) sec.getDouble("saturation", 5.0);
                int expLvl = sec.getInt("exp-level", 0);
                float expPts = (float) sec.getDouble("exp-points", 0.0);
                String gmStr = sec.getString("gamemode", "SURVIVAL");
                GameMode gm = GameMode.valueOf(gmStr);
                long last = sec.getLong("last-sync", System.currentTimeMillis());

                syncCache.put(uuid, new SyncData(inv, ec, hp, food, sat, expLvl, expPts, gm, last));
            } catch (Exception ignored) {}
        }
        plugin.getLogger().info("Loaded " + syncCache.size() + " synchronized player records.");
    }

    public synchronized void saveData() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection root = config.createSection("data");

        for (Map.Entry<UUID, SyncData> entry : syncCache.entrySet()) {
            String uStr = entry.getKey().toString();
            SyncData d = entry.getValue();
            root.set(uStr + ".inventory", d.getInventoryBase64());
            root.set(uStr + ".enderchest", d.getEnderChestBase64());
            root.set(uStr + ".health", d.getHealth());
            root.set(uStr + ".food", d.getFoodLevel());
            root.set(uStr + ".saturation", d.getSaturation());
            root.set(uStr + ".exp-level", d.getExpLevel());
            root.set(uStr + ".exp-points", d.getExpPoints());
            root.set(uStr + ".gamemode", d.getGameMode().name());
            root.set(uStr + ".last-sync", d.getLastSync());
        }

        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save sync storage data", e);
        }
    }

    public void savePlayer(Player player) {
        String inv = SyncSerializer.itemStackArrayToBase64(player.getInventory().getContents());
        String ec = SyncSerializer.itemStackArrayToBase64(player.getEnderChest().getContents());
        double hp = Math.min(player.getHealth(), player.getMaxHealth());
        int food = player.getFoodLevel();
        float sat = player.getSaturation();
        int expLvl = player.getLevel();
        float expPts = player.getExp();
        GameMode gm = player.getGameMode();

        SyncData data = new SyncData(inv, ec, hp, food, sat, expLvl, expPts, gm, System.currentTimeMillis());
        syncCache.put(player.getUniqueId(), data);
        saveData();
    }

    public boolean applyPlayer(Player player) {
        SyncData data = syncCache.get(player.getUniqueId());
        if (data == null) return false;

        try {
            if (!data.getInventoryBase64().isEmpty()) {
                ItemStack[] items = SyncSerializer.itemStackArrayFromBase64(data.getInventoryBase64());
                player.getInventory().setContents(items);
            }
            if (!data.getEnderChestBase64().isEmpty()) {
                ItemStack[] ecItems = SyncSerializer.itemStackArrayFromBase64(data.getEnderChestBase64());
                player.getEnderChest().setContents(ecItems);
            }
            player.setHealth(Math.min(data.getHealth(), player.getMaxHealth()));
            player.setFoodLevel(data.getFoodLevel());
            player.setSaturation(data.getSaturation());
            player.setLevel(data.getExpLevel());
            player.setExp(data.getExpPoints());
            player.setGameMode(data.getGameMode());
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to apply sync state for " + player.getName(), e);
            return false;
        }
    }

    public SyncData getSyncData(UUID uuid) {
        return syncCache.get(uuid);
    }
}
