package dev.nexoria.particles;

import java.util.UUID;

/**
 * Public developer API for NexoriaParticles.
 */
public class ParticlesAPI {

    private final NexoriaParticlesPlugin plugin;

    public ParticlesAPI(NexoriaParticlesPlugin plugin) {
        this.plugin = plugin;
    }

    public void setEffect(UUID uuid, String effect) {
        plugin.getParticleManager().setEffect(uuid, effect);
    }

    public void clear(UUID uuid) {
        plugin.getParticleManager().clear(uuid);
    }

    public String getEffect(UUID uuid) {
        return plugin.getParticleManager().getEffect(uuid);
    }

    public ParticleManager getManager() {
        return plugin.getParticleManager();
    }
}
