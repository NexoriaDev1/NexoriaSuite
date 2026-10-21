package dev.nexoria.sync;

import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.UUID;

public final class SyncAPI {

    public record ServiceHealth(String service, boolean active, Instant timestamp) {}

    private final NexoriaSyncPlugin plugin;

    public SyncAPI(NexoriaSyncPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isActive() {
        return plugin.isEnabled();
    }

    public ServiceHealth getStatus() {
        return new ServiceHealth("NexoriaSync", isActive(), Instant.now());
    }

    public void savePlayer(Player player) {
        plugin.getSyncManager().savePlayer(player);
    }

    public boolean applyPlayer(Player player) {
        return plugin.getSyncManager().applyPlayer(player);
    }

    public SyncData getSyncData(UUID uuid) {
        return plugin.getSyncManager().getSyncData(uuid);
    }

    protected void shutdown() {}

    public NexoriaSyncPlugin getPlugin() {
        return plugin;
    }
}
