package dev.nexoria.vanish;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe Vanish API managing hidden entities.
 */
public class VanishAPI {

    public record ServiceHealth(String service, boolean active, int totalVanished, Instant timestamp) {}

    private final NexoriaVanishPlugin plugin;
    private final Set<UUID> vanishedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private volatile boolean active;

    public VanishAPI(NexoriaVanishPlugin plugin) {
        this.plugin = plugin;
        this.active = true;
    }

    public boolean isActive() {
        return active;
    }

    public ServiceHealth getStatus() {
        return new ServiceHealth("NexoriaVanish", active, vanishedPlayers.size(), Instant.now());
    }

    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }

    public void setVanished(Player player, boolean vanish) {
        UUID uuid = player.getUniqueId();
        if (vanish) {
            if (vanishedPlayers.add(uuid)) {
                applyVanish(player);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.now-vanished", "&7You are now &aVanished &7(Hidden from normal players).")));
            }
        } else {
            if (vanishedPlayers.remove(uuid)) {
                removeVanish(player);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.no-longer-vanished", "&7You are no longer &cVanished &7(Visible to everyone).")));
            }
        }
    }

    public void applyVanish(Player player) {
        for (Player other : plugin.getServer().getOnlinePlayers()) {
            if (other.equals(player)) continue;
            if (!other.hasPermission("nexoria.vanish.see")) {
                other.hidePlayer(plugin, player);
            }
        }
    }

    public void removeVanish(Player player) {
        for (Player other : plugin.getServer().getOnlinePlayers()) {
            if (other.equals(player)) continue;
            other.showPlayer(plugin, player);
        }
    }

    public void updateForJoin(Player joiningPlayer) {
        boolean canSee = joiningPlayer.hasPermission("nexoria.vanish.see");
        for (UUID vId : vanishedPlayers) {
            Player vPlayer = plugin.getServer().getPlayer(vId);
            if (vPlayer != null && !canSee) {
                joiningPlayer.hidePlayer(plugin, vPlayer);
            }
        }
    }

    public Set<UUID> getVanishedPlayers() {
        return Collections.unmodifiableSet(vanishedPlayers);
    }

    public void invalidateSession(UUID uuid) {
        vanishedPlayers.remove(uuid);
    }

    protected void shutdown() {
        this.active = false;
        for (UUID uuid : vanishedPlayers) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null) {
                removeVanish(p);
            }
        }
        vanishedPlayers.clear();
    }
}
