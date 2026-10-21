package dev.nexoria.nbt;

import org.bukkit.inventory.ItemStack;
import java.util.Map;

/**
 * Public developer API for NexoriaNBT.
 */
public class NBTAPI {

    private final NexoriaNBTPlugin plugin;

    public NBTAPI(NexoriaNBTPlugin plugin) {
        this.plugin = plugin;
    }

    public void setString(ItemStack item, String key, String value) {
        plugin.getNBTManager().setString(item, key, value);
    }

    public String getString(ItemStack item, String key) {
        return plugin.getNBTManager().getString(item, key);
    }

    public void setInt(ItemStack item, String key, int value) {
        plugin.getNBTManager().setInt(item, key, value);
    }

    public Integer getInt(ItemStack item, String key) {
        return plugin.getNBTManager().getInt(item, key);
    }

    public void remove(ItemStack item, String key) {
        plugin.getNBTManager().remove(item, key);
    }

    public Map<String, String> getAllTags(ItemStack item) {
        return plugin.getNBTManager().getAllTags(item);
    }

    public NBTManager getManager() {
        return plugin.getNBTManager();
    }
}
