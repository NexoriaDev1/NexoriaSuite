package dev.nexoria.customcraft;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class CustomCraftListener implements Listener {

    private final NexoriaCustomCraftPlugin plugin;

    public CustomCraftListener(NexoriaCustomCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            player.discoverRecipes(plugin.getRecipeManager().getRegisteredKeys());
        } catch (Throwable ignored) {}
    }
}
