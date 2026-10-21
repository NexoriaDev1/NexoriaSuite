package dev.nexoria.worldguard;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.List;

public final class WorldGuardBridgeAPI {

    public record ServiceHealth(String service, boolean active, Instant timestamp, boolean hooked) {}

    private final NexoriaWorldGuardBridgePlugin plugin;

    public WorldGuardBridgeAPI(NexoriaWorldGuardBridgePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isActive() {
        return plugin.isEnabled();
    }

    public ServiceHealth getStatus() {
        boolean hooked = plugin.getHook() != null && plugin.getHook().isAvailable();
        return new ServiceHealth("NexoriaWorldGuardBridge", isActive(), Instant.now(), hooked);
    }

    public boolean isWorldGuardPresent() {
        return plugin.getHook().isAvailable();
    }

    public boolean canBuild(Player player, Location location) {
        return plugin.getHook().canBuild(player, location);
    }

    public boolean canPvP(Player player, Location location) {
        return plugin.getHook().canPvP(player, location);
    }

    public List<String> getRegions(Location location) {
        return plugin.getHook().getRegions(location);
    }

    protected void shutdown() {}

    public NexoriaWorldGuardBridgePlugin getPlugin() {
        return plugin;
    }
}
