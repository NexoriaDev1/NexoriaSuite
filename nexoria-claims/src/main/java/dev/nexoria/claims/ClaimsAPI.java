package dev.nexoria.claims;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Developer API contract for NexoriaClaims.
 */
public final class ClaimsAPI {

    public record ServiceHealth(String service, boolean active, Instant timestamp, int totalClaims) {}

    private final NexoriaClaimsPlugin plugin;

    public ClaimsAPI(NexoriaClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isActive() {
        return plugin.isEnabled();
    }

    public ServiceHealth getStatus() {
        int count = plugin.getClaimManager() != null ? plugin.getClaimManager().getAllClaims().size() : 0;
        return new ServiceHealth("NexoriaClaims", isActive(), Instant.now(), count);
    }

    public Claim getClaim(Location location) {
        return plugin.getClaimManager().getClaim(location);
    }

    public Claim getClaim(Chunk chunk) {
        return plugin.getClaimManager().getClaim(chunk);
    }

    public boolean isClaimed(Location location) {
        return plugin.getClaimManager().isClaimed(location);
    }

    public boolean canAccess(Player player, Location location) {
        return plugin.getClaimManager().canAccess(player, location);
    }

    public List<Claim> getPlayerClaims(UUID playerUuid) {
        return plugin.getClaimManager().getClaims(playerUuid);
    }

    public int getClaimsCount() {
        return plugin.getClaimManager().getAllClaims().size();
    }

    protected void shutdown() {
        // Cleanup handled by manager
    }

    public NexoriaClaimsPlugin getPlugin() {
        return plugin;
    }
}
