package dev.nexoria.sync;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Primary event listener for NexoriaSync.
 */
public final class SyncListener implements Listener {

    private final NexoriaSyncPlugin plugin;

    public SyncListener(NexoriaSyncPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getApi().isActive()) {
            return;
        }
        var player = event.getPlayer();
        plugin.getApi().registerSession(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        plugin.getApi().invalidateSession(player.getUniqueId());
    }
}
