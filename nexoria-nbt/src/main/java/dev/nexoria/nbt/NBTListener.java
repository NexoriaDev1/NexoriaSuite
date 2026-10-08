package dev.nexoria.nbt;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Primary event listener for NexoriaNBT.
 */
public final class NBTListener implements Listener {

    private final NexoriaNBTPlugin plugin;

    public NBTListener(NexoriaNBTPlugin plugin) {
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
