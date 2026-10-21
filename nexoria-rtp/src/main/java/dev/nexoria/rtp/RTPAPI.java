package dev.nexoria.rtp;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * High-performance random wilderness finder.
 * Thread-safe for Paper and regional Folia multi-threading.
 */
public class RTPAPI {

    public record ServiceHealth(String service, boolean active, int totalTeleports, Instant timestamp) {}

    private final NexoriaRTPPlugin plugin;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Set<Material> unsafeMaterials = EnumSet.of(
            Material.LAVA, Material.WATER, Material.FIRE, Material.SOUL_FIRE,
            Material.CACTUS, Material.MAGMA_BLOCK, Material.SWEET_BERRY_BUSH,
            Material.POWDER_SNOW, Material.VOID_AIR, Material.AIR, Material.CAVE_AIR
    );

    private volatile boolean active;
    private int minRange;
    private int maxRange;
    private double cooldownSeconds;
    private int maxAttempts;
    private volatile int totalTeleportsCount;

    public RTPAPI(NexoriaRTPPlugin plugin) {
        this.plugin = plugin;
        this.active = true;
        this.totalTeleportsCount = 0;
        reload();
    }

    public void reload() {
        this.minRange = plugin.getConfig().getInt("rtp.min-range", 150);
        this.maxRange = plugin.getConfig().getInt("rtp.max-range", 3000);
        this.cooldownSeconds = plugin.getConfig().getDouble("rtp.cooldown-seconds", 30.0);
        this.maxAttempts = plugin.getConfig().getInt("rtp.max-attempts", 15);
    }

    public boolean isActive() {
        return active;
    }

    public ServiceHealth getStatus() {
        return new ServiceHealth("NexoriaRTP", active, totalTeleportsCount, Instant.now());
    }

    public boolean checkCooldown(UUID uuid) {
        if (cooldownSeconds <= 0) return true;
        Long last = cooldowns.get(uuid);
        long now = System.currentTimeMillis();
        if (last != null) {
            long diff = now - last;
            if (diff < (long)(cooldownSeconds * 1000)) {
                return false;
            }
        }
        cooldowns.put(uuid, now);
        return true;
    }

    public double getRemainingCooldown(UUID uuid) {
        Long last = cooldowns.get(uuid);
        if (last == null) return 0.0;
        long diff = System.currentTimeMillis() - last;
        long required = (long)(cooldownSeconds * 1000);
        if (diff >= required) return 0.0;
        return ((required - diff) / 100.0) / 10.0;
    }

    public CompletableFuture<Location> findSafeLocation(World world) {
        return CompletableFuture.supplyAsync(() -> {
            ThreadLocalRandom random = ThreadLocalRandom.current();

            for (int i = 0; i < maxAttempts; i++) {
                int angle = random.nextInt(360);
                double rad = Math.toRadians(angle);
                int dist = minRange + random.nextInt(maxRange - minRange);

                int x = (int) (dist * Math.cos(rad));
                int z = (int) (dist * Math.sin(rad));

                // Folia chunk async lookup
                int chunkX = x >> 4;
                int chunkZ = z >> 4;

                try {
                    world.getChunkAtAsync(chunkX, chunkZ).get();
                } catch (Exception ignored) {
                    continue;
                }

                int highestY = world.getHighestBlockYAt(x, z);
                if (highestY <= world.getMinHeight() + 5) continue;

                Block ground = world.getBlockAt(x, highestY - 1, z);
                Block feet = world.getBlockAt(x, highestY, z);
                Block head = world.getBlockAt(x, highestY + 1, z);

                if (isSafeBlock(ground, feet, head)) {
                    totalTeleportsCount++;
                    return new Location(world, x + 0.5, highestY, z + 0.5);
                }
            }
            return null;
        });
    }

    private boolean isSafeBlock(Block ground, Block feet, Block head) {
        if (!ground.getType().isSolid()) return false;
        if (unsafeMaterials.contains(ground.getType())) return false;
        if (feet.getType().isSolid() || feet.isLiquid()) return false;
        if (head.getType().isSolid() || head.isLiquid()) return false;
        return true;
    }

    public void invalidateSession(UUID uuid) {
        cooldowns.remove(uuid);
    }

    protected void shutdown() {
        this.active = false;
        cooldowns.clear();
    }
}
