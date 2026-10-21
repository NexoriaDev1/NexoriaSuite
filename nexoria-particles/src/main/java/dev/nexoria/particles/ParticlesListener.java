package dev.nexoria.particles;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ParticlesListener implements Listener {

    private final NexoriaParticlesPlugin plugin;

    public ParticlesListener(NexoriaParticlesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getParticleManager().clear(event.getPlayer().getUniqueId());
    }
}
