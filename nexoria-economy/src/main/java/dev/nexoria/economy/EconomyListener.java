package dev.nexoria.economy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginEnableEvent;

public final class EconomyListener implements Listener {

    private final NexoriaEconomyPlugin plugin;

    public EconomyListener(NexoriaEconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getApi().isActive()) {
            return;
        }
        var player = event.getPlayer();
        plugin.getApi().updatePlayerName(player.getUniqueId(), player.getName());
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if ("Vault".equalsIgnoreCase(event.getPlugin().getName())) {
            VaultHook.register(plugin);
        }
    }
}
