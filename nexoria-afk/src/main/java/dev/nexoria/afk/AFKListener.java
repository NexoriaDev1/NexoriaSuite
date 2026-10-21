package dev.nexoria.afk;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

/**
 * Listener capturing player actions to accurately reset idle counters.
 */
public final class AFKListener implements Listener {

    private final NexoriaAFKPlugin plugin;

    public AFKListener(NexoriaAFKPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getApi() != null) {
            plugin.getApi().registerSession(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getApi() != null) {
            plugin.getApi().invalidateSession(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfig().getBoolean("afk.cancel-on-move", true)) return;
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        // Only register as activity if actual block coordinates changed (ignore head turns)
        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            if (plugin.getApi() != null) {
                plugin.getApi().recordActivity(event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfig().getBoolean("afk.cancel-on-chat", true)) return;
        if (plugin.getApi() != null) {
            plugin.getApi().recordActivity(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getConfig().getBoolean("afk.cancel-on-interact", true)) return;
        if (plugin.getApi() != null) {
            plugin.getApi().recordActivity(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        // Exclude running /afk itself from cancelling AFK prematurely
        String msg = event.getMessage().toLowerCase();
        if (msg.startsWith("/afk") || msg.startsWith("/away")) return;

        if (plugin.getApi() != null) {
            plugin.getApi().recordActivity(event.getPlayer());
        }
    }
}
