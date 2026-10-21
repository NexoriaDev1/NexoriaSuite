package dev.nexoria.particles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ParticleManager {

    private final NexoriaParticlesPlugin plugin;
    private final Map<UUID, String> activeEffects = new ConcurrentHashMap<>();
    private BukkitTask task;
    private double tickAngle = 0.0;

    public ParticleManager(NexoriaParticlesPlugin plugin) {
        this.plugin = plugin;
        startTask();
    }

    public void setEffect(UUID uuid, String effect) {
        if ("clear".equalsIgnoreCase(effect)) {
            activeEffects.remove(uuid);
        } else {
            activeEffects.put(uuid, effect.toLowerCase());
        }
    }

    public void clear(UUID uuid) {
        activeEffects.remove(uuid);
    }

    public String getEffect(UUID uuid) {
        return activeEffects.get(uuid);
    }

    public void startTask() {
        if (task != null) {
            task.cancel();
        }
        long interval = Math.max(1L, plugin.getConfig().getLong("settings.animation-interval-ticks", 3L));
        this.task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 10L, interval);
    }

    private void tick() {
        tickAngle += 0.3;
        if (tickAngle > Math.PI * 2) {
            tickAngle = 0.0;
        }

        for (Map.Entry<UUID, String> entry : activeEffects.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                continue;
            }

            String effect = entry.getValue();
            Location loc = player.getLocation();

            switch (effect) {
                case "halo" -> spawnHalo(loc);
                case "orbit" -> spawnOrbit(loc);
                case "trail" -> spawnTrail(loc);
                case "wings" -> spawnWings(loc);
            }
        }
    }

    private void spawnHalo(Location loc) {
        double r = plugin.getConfig().getDouble("effects.halo.radius", 0.35);
        double yOff = plugin.getConfig().getDouble("effects.halo.offset-y", 2.1);
        int points = plugin.getConfig().getInt("effects.halo.count", 10);
        Particle p = getParticle("effects.halo.particle", Particle.FLAME);

        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double x = Math.cos(angle) * r;
            double z = Math.sin(angle) * r;
            loc.getWorld().spawnParticle(p, loc.getX() + x, loc.getY() + yOff, loc.getZ() + z, 1, 0, 0, 0, 0);
        }
    }

    private void spawnOrbit(Location loc) {
        double r = plugin.getConfig().getDouble("effects.orbit.radius", 0.9);
        Particle p = getParticle("effects.orbit.particle", Particle.END_ROD);

        double x1 = Math.cos(tickAngle) * r;
        double z1 = Math.sin(tickAngle) * r;
        double x2 = Math.cos(tickAngle + Math.PI) * r;
        double z2 = Math.sin(tickAngle + Math.PI) * r;

        loc.getWorld().spawnParticle(p, loc.getX() + x1, loc.getY() + 1.0, loc.getZ() + z1, 1, 0, 0, 0, 0);
        loc.getWorld().spawnParticle(p, loc.getX() + x2, loc.getY() + 1.0, loc.getZ() + z2, 1, 0, 0, 0, 0);
    }

    private void spawnTrail(Location loc) {
        Particle p = getParticle("effects.trail.particle", Particle.CHERRY_LEAVES);
        loc.getWorld().spawnParticle(p, loc.getX(), loc.getY() + 0.1, loc.getZ(), 2, 0.1, 0.05, 0.1, 0.01);
    }

    private void spawnWings(Location loc) {
        Particle p = getParticle("effects.wings.particle", Particle.SOUL_FIRE_FLAME);
        double yawRad = Math.toRadians(-loc.getYaw() - 90);

        for (double i = 0.2; i <= 0.8; i += 0.2) {
            double xOff = Math.cos(yawRad) * i;
            double zOff = Math.sin(yawRad) * i;
            loc.getWorld().spawnParticle(p, loc.getX() + xOff, loc.getY() + 1.2 + (i * 0.5), loc.getZ() + zOff, 1, 0, 0, 0, 0);
            loc.getWorld().spawnParticle(p, loc.getX() - xOff, loc.getY() + 1.2 + (i * 0.5), loc.getZ() - zOff, 1, 0, 0, 0, 0);
        }
    }

    private Particle getParticle(String configPath, Particle def) {
        String name = plugin.getConfig().getString(configPath);
        if (name == null) return def;
        try {
            return Particle.valueOf(name.toUpperCase());
        } catch (Exception e) {
            return def;
        }
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
        }
        activeEffects.clear();
    }
}
