package dev.nexoria.customitems;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class CustomItemManager {

    private final NexoriaCustomItemsPlugin plugin;
    private final Map<String, CustomItem> items = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();
    private final NamespacedKey itemKey;

    public CustomItemManager(NexoriaCustomItemsPlugin plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "custom_item_id");
        loadItems();
    }

    public synchronized void loadItems() {
        items.clear();
        File file = new File(plugin.getDataFolder(), "items.yml");
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try (InputStream in = plugin.getResource("items.yml")) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not save default items.yml", e);
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = config.getConfigurationSection("items");
        if (root == null) {
            registerDefaults();
            return;
        }

        for (String key : root.getKeys(false)) {
            try {
                ConfigurationSection sec = root.getConfigurationSection(key);
                if (sec == null) continue;

                String matStr = sec.getString("material", "STICK").toUpperCase();
                Material mat = Material.matchMaterial(matStr);
                if (mat == null) mat = Material.STICK;

                String name = sec.getString("name", key);
                List<String> lore = sec.getStringList("lore");
                int cmd = sec.getInt("custom-model-data", 0);
                boolean unbreakable = sec.getBoolean("unbreakable", false);
                String ability = sec.getString("ability", "NONE");
                int cd = sec.getInt("cooldown", 0);

                Map<Enchantment, Integer> enchants = new HashMap<>();
                ConfigurationSection enchSec = sec.getConfigurationSection("enchantments");
                if (enchSec != null) {
                    for (String eKey : enchSec.getKeys(false)) {
                        try {
                            Enchantment ench = Enchantment.getByName(eKey.toUpperCase());
                            if (ench != null) {
                                enchants.put(ench, enchSec.getInt(eKey));
                            }
                        } catch (Exception ignored) {}
                    }
                }

                Set<ItemFlag> flags = new HashSet<>();
                for (String fStr : sec.getStringList("flags")) {
                    try {
                        flags.add(ItemFlag.valueOf(fStr.toUpperCase()));
                    } catch (Exception ignored) {}
                }

                CustomItem item = new CustomItem(key, mat, name, lore, cmd, enchants, flags, unbreakable, ability, cd);
                items.put(key.toLowerCase(), item);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load custom item: " + key, e);
            }
        }

        if (items.isEmpty()) {
            registerDefaults();
        }

        plugin.getLogger().info("Loaded " + items.size() + " custom items.");
    }

    private void registerDefaults() {
        CustomItem thunderBlade = new CustomItem(
                "thunder_blade",
                Material.DIAMOND_SWORD,
                "&b&lThunder Blade",
                List.of("&7Strikes lightning on target hit.", "&8Cooldown: 5s"),
                1001,
                Map.of(Enchantment.SHARPNESS, 4),
                Set.of(ItemFlag.HIDE_ENCHANTS),
                false,
                "LIGHTNING",
                5
        );
        CustomItem teleportWand = new CustomItem(
                "teleport_wand",
                Material.BLAZE_ROD,
                "&d&lTeleport Wand",
                List.of("&7Right click to leap forward instantly.", "&8Cooldown: 3s"),
                1002,
                Collections.emptyMap(),
                Collections.emptySet(),
                true,
                "TELEPORT",
                3
        );
        CustomItem healingAura = new CustomItem(
                "healing_totem",
                Material.TOTEM_OF_UNDYING,
                "&a&lHealing Totem",
                List.of("&7Right click to regenerate health.", "&8Cooldown: 30s"),
                1003,
                Collections.emptyMap(),
                Collections.emptySet(),
                false,
                "HEAL",
                30
        );
        items.put(thunderBlade.getId(), thunderBlade);
        items.put(teleportWand.getId(), teleportWand);
        items.put(healingAura.getId(), healingAura);
    }

    public CustomItem getItem(String id) {
        if (id == null) return null;
        return items.get(id.toLowerCase());
    }

    public String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
    }

    public boolean isCustomItem(ItemStack item) {
        return getItemId(item) != null;
    }

    public CustomItem getCustomItem(ItemStack item) {
        String id = getItemId(item);
        if (id == null) return null;
        return getItem(id);
    }

    public boolean isOnCooldown(UUID uuid, String itemId) {
        Map<String, Long> playerCds = cooldowns.get(uuid);
        if (playerCds == null) return false;
        Long expire = playerCds.get(itemId.toLowerCase());
        if (expire == null) return false;
        if (System.currentTimeMillis() >= expire) {
            playerCds.remove(itemId.toLowerCase());
            return false;
        }
        return true;
    }

    public long getCooldownRemainingSeconds(UUID uuid, String itemId) {
        Map<String, Long> playerCds = cooldowns.get(uuid);
        if (playerCds == null) return 0;
        Long expire = playerCds.get(itemId.toLowerCase());
        if (expire == null) return 0;
        long rem = (expire - System.currentTimeMillis()) / 1000L;
        return Math.max(0, rem);
    }

    public void setCooldown(UUID uuid, String itemId, int seconds) {
        if (seconds <= 0) return;
        cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(itemId.toLowerCase(), System.currentTimeMillis() + (seconds * 1000L));
    }

    public Map<String, CustomItem> getAllItems() {
        return Collections.unmodifiableMap(items);
    }

    public NamespacedKey getItemKey() {
        return itemKey;
    }
}
