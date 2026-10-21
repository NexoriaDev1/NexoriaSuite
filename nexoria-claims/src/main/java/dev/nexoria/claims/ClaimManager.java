package dev.nexoria.claims;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class ClaimManager {

    private final NexoriaClaimsPlugin plugin;
    private final Map<String, Claim> claims = new ConcurrentHashMap<>();
    private final File claimsFile;

    public ClaimManager(NexoriaClaimsPlugin plugin) {
        this.plugin = plugin;
        this.claimsFile = new File(plugin.getDataFolder(), "claims.yml");
        loadClaims();
    }

    public synchronized void loadClaims() {
        claims.clear();
        if (!claimsFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(claimsFile);
        ConfigurationSection section = config.getConfigurationSection("claims");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                String world = section.getString(key + ".world");
                int chunkX = section.getInt(key + ".chunk-x");
                int chunkZ = section.getInt(key + ".chunk-z");
                String ownerUuidStr = section.getString(key + ".owner");
                if (world == null || ownerUuidStr == null) continue;

                UUID owner = UUID.fromString(ownerUuidStr);
                String ownerName = section.getString(key + ".owner-name", "Unknown");
                List<String> rawTrusted = section.getStringList(key + ".trusted");
                Set<UUID> trusted = new HashSet<>();
                for (String t : rawTrusted) {
                    try {
                        trusted.add(UUID.fromString(t));
                    } catch (IllegalArgumentException ignored) {}
                }

                long createdAt = section.getLong(key + ".created-at", System.currentTimeMillis());
                boolean pvp = section.getBoolean(key + ".flags.pvp", false);
                boolean explosions = section.getBoolean(key + ".flags.explosions", false);

                Claim claim = new Claim(world, chunkX, chunkZ, owner, ownerName, trusted, createdAt, pvp, explosions);
                claims.put(claim.getKey(), claim);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load claim: " + key, e);
            }
        }
        plugin.getLogger().info("Loaded " + claims.size() + " claims.");
    }

    public synchronized void saveClaims() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection root = config.createSection("claims");

        int index = 0;
        for (Claim claim : claims.values()) {
            String node = "c" + (index++);
            root.set(node + ".world", claim.getWorld());
            root.set(node + ".chunk-x", claim.getChunkX());
            root.set(node + ".chunk-z", claim.getChunkZ());
            root.set(node + ".owner", claim.getOwner().toString());
            root.set(node + ".owner-name", claim.getOwnerName());
            List<String> trustedStr = new ArrayList<>();
            for (UUID u : claim.getTrustedUsers()) {
                trustedStr.add(u.toString());
            }
            root.set(node + ".trusted", trustedStr);
            root.set(node + ".created-at", claim.getCreatedAt());
            root.set(node + ".flags.pvp", claim.isPvp());
            root.set(node + ".flags.explosions", claim.isExplosions());
        }

        try {
            config.save(claimsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save claims to claims.yml", e);
        }
    }

    public Claim getClaim(World world, int chunkX, int chunkZ) {
        if (world == null) return null;
        return claims.get(world.getName() + ";" + chunkX + ";" + chunkZ);
    }

    public Claim getClaim(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        return getClaim(loc.getWorld(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    public Claim getClaim(Chunk chunk) {
        if (chunk == null) return null;
        return getClaim(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    public boolean isClaimed(Chunk chunk) {
        return getClaim(chunk) != null;
    }

    public boolean isClaimed(Location loc) {
        return getClaim(loc) != null;
    }

    public int getPlayerClaimCount(UUID uuid) {
        int count = 0;
        for (Claim c : claims.values()) {
            if (c.getOwner().equals(uuid)) count++;
        }
        return count;
    }

    public int getMaxClaims(Player player) {
        if (player.hasPermission("nexoria.claims.limit.unlimited") || player.isOp()) {
            return 1000;
        }
        for (int i = 100; i >= 1; i--) {
            if (player.hasPermission("nexoria.claims.limit." + i)) {
                return i;
            }
        }
        return plugin.getConfig().getInt("default-max-claims", 4);
    }

    public boolean canClaim(Player player, Chunk chunk) {
        if (isClaimed(chunk)) return false;
        int max = getMaxClaims(player);
        int current = getPlayerClaimCount(player.getUniqueId());
        return current < max;
    }

    public Claim createClaim(Player player, Chunk chunk) {
        Claim claim = new Claim(
                chunk.getWorld().getName(),
                chunk.getX(),
                chunk.getZ(),
                player.getUniqueId(),
                player.getName(),
                new HashSet<>(),
                System.currentTimeMillis(),
                false,
                false
        );
        claims.put(claim.getKey(), claim);
        saveClaims();
        return claim;
    }

    public boolean unclaim(World world, int chunkX, int chunkZ) {
        Claim removed = claims.remove(world.getName() + ";" + chunkX + ";" + chunkZ);
        if (removed != null) {
            saveClaims();
            return true;
        }
        return false;
    }

    public boolean canAccess(Player player, Location location) {
        if (player.hasPermission("nexoria.claims.admin.bypass")) return true;
        Claim claim = getClaim(location);
        if (claim == null) return true;
        return claim.isTrusted(player.getUniqueId());
    }

    public List<Claim> getClaims(UUID owner) {
        List<Claim> result = new ArrayList<>();
        for (Claim c : claims.values()) {
            if (c.getOwner().equals(owner)) {
                result.add(c);
            }
        }
        return result;
    }

    public Map<String, Claim> getAllClaims() {
        return Collections.unmodifiableMap(claims);
    }
}
