package dev.nexoria.loot;

import java.time.Instant;
import java.util.List;

public final class LootAPI {

    public record ServiceHealth(String service, boolean active, Instant timestamp, int mobTables, int blockTables) {}

    private final NexoriaLootPlugin plugin;

    public LootAPI(NexoriaLootPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isActive() {
        return plugin.isEnabled();
    }

    public ServiceHealth getStatus() {
        int m = plugin.getLootManager() != null ? plugin.getLootManager().getAllEntityDrops().size() : 0;
        int b = plugin.getLootManager() != null ? plugin.getLootManager().getAllBlockDrops().size() : 0;
        return new ServiceHealth("NexoriaLoot", isActive(), Instant.now(), m, b);
    }

    public List<LootEntry> getEntityDrops(String entityType) {
        return plugin.getLootManager().getEntityDrops(entityType);
    }

    public List<LootEntry> getBlockDrops(String blockType) {
        return plugin.getLootManager().getBlockDrops(blockType);
    }

    protected void shutdown() {}

    public NexoriaLootPlugin getPlugin() {
        return plugin;
    }
}
