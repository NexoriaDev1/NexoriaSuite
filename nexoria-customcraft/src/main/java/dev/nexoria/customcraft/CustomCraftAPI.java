package dev.nexoria.customcraft;

import org.bukkit.NamespacedKey;

import java.time.Instant;
import java.util.Set;

public final class CustomCraftAPI {

    public record ServiceHealth(String service, boolean active, Instant timestamp, int totalRecipes) {}

    private final NexoriaCustomCraftPlugin plugin;

    public CustomCraftAPI(NexoriaCustomCraftPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isActive() {
        return plugin.isEnabled();
    }

    public ServiceHealth getStatus() {
        int count = plugin.getRecipeManager() != null ? plugin.getRecipeManager().getRegisteredKeys().size() : 0;
        return new ServiceHealth("NexoriaCustomCraft", isActive(), Instant.now(), count);
    }

    public Set<NamespacedKey> getRegisteredRecipes() {
        return plugin.getRecipeManager().getRegisteredKeys();
    }

    protected void shutdown() {}

    public NexoriaCustomCraftPlugin getPlugin() {
        return plugin;
    }
}
