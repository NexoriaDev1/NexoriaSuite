package dev.nexoria.warps;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Event listener cleaning up cache on quit.
 */
public final class WarpsListener implements Listener {

    private final NexoriaWarpsPlugin plugin;

    public WarpsListener(NexoriaWarpsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getApi() != null) {
            plugin.getApi().invalidateSession(event.getPlayer().getUniqueId());
        }
    }
}
