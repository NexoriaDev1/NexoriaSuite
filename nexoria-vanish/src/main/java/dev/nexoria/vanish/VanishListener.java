package dev.nexoria.vanish;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Event listener enforcing vanish rules like mob targeting and pickup block.
 */
public final class VanishListener implements Listener {

    private final NexoriaVanishPlugin plugin;

    public VanishListener(NexoriaVanishPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getApi() != null) {
            plugin.getApi().updateForJoin(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getApi() != null) {
            plugin.getApi().invalidateSession(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if (!plugin.getConfig().getBoolean("vanish.prevent-mob-targeting", true)) return;
        if (event.getTarget() instanceof Player p) {
            if (plugin.getApi() != null && plugin.getApi().isVanished(p.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!plugin.getConfig().getBoolean("vanish.prevent-item-pickup", true)) return;
        if (event.getEntity() instanceof Player p) {
            if (plugin.getApi() != null && plugin.getApi().isVanished(p.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
