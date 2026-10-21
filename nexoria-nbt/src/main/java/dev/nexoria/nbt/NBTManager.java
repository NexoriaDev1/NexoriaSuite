package dev.nexoria.nbt;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class NBTManager {

    private final NexoriaNBTPlugin plugin;

    public NBTManager(NexoriaNBTPlugin plugin) {
        this.plugin = plugin;
    }

    public void setString(ItemStack item, String key, String value) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey nk = new NamespacedKey(plugin, key.toLowerCase());
            meta.getPersistentDataContainer().set(nk, PersistentDataType.STRING, value);
            item.setItemMeta(meta);
        }
    }

    public String getString(ItemStack item, String key) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey nk = new NamespacedKey(plugin, key.toLowerCase());
            return meta.getPersistentDataContainer().get(nk, PersistentDataType.STRING);
        }
        return null;
    }

    public void setInt(ItemStack item, String key, int value) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey nk = new NamespacedKey(plugin, key.toLowerCase());
            meta.getPersistentDataContainer().set(nk, PersistentDataType.INTEGER, value);
            item.setItemMeta(meta);
        }
    }

    public Integer getInt(ItemStack item, String key) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey nk = new NamespacedKey(plugin, key.toLowerCase());
            return meta.getPersistentDataContainer().get(nk, PersistentDataType.INTEGER);
        }
        return null;
    }

    public void remove(ItemStack item, String key) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey nk = new NamespacedKey(plugin, key.toLowerCase());
            meta.getPersistentDataContainer().remove(nk);
            item.setItemMeta(meta);
        }
    }

    public Map<String, String> getAllTags(ItemStack item) {
        Map<String, String> result = new HashMap<>();
        if (item == null) return result;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            for (NamespacedKey nk : pdc.getKeys()) {
                if (pdc.has(nk, PersistentDataType.STRING)) {
                    result.put(nk.toString(), pdc.get(nk, PersistentDataType.STRING));
                } else if (pdc.has(nk, PersistentDataType.INTEGER)) {
                    result.put(nk.toString(), String.valueOf(pdc.get(nk, PersistentDataType.INTEGER)));
                } else if (pdc.has(nk, PersistentDataType.DOUBLE)) {
                    result.put(nk.toString(), String.valueOf(pdc.get(nk, PersistentDataType.DOUBLE)));
                } else {
                    result.put(nk.toString(), "[binary data]");
                }
            }
        }
        return result;
    }
}
