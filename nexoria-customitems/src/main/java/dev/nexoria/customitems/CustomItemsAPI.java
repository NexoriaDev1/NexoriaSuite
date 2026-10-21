package dev.nexoria.customitems;

import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.Collection;

public final class CustomItemsAPI {

    public record ServiceHealth(String service, boolean active, Instant timestamp, int totalItems) {}

    private final NexoriaCustomItemsPlugin plugin;

    public CustomItemsAPI(NexoriaCustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isActive() {
        return plugin.isEnabled();
    }

    public ServiceHealth getStatus() {
        int count = plugin.getItemManager() != null ? plugin.getItemManager().getAllItems().size() : 0;
        return new ServiceHealth("NexoriaCustomItems", isActive(), Instant.now(), count);
    }

    public ItemStack getItem(String id, int amount) {
        CustomItem ci = plugin.getItemManager().getItem(id);
        if (ci == null) return null;
        return ci.createItemStack(plugin, amount);
    }

    public CustomItem getCustomItem(String id) {
        return plugin.getItemManager().getItem(id);
    }

    public boolean isCustomItem(ItemStack item) {
        return plugin.getItemManager().isCustomItem(item);
    }

    public String getItemId(ItemStack item) {
        return plugin.getItemManager().getItemId(item);
    }

    public Collection<CustomItem> getAllItems() {
        return plugin.getItemManager().getAllItems().values();
    }

    protected void shutdown() {}

    public NexoriaCustomItemsPlugin getPlugin() {
        return plugin;
    }
}
