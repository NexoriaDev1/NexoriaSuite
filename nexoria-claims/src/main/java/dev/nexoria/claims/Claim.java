package dev.nexoria.claims;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a claimed chunk in the game world.
 */
public final class Claim {

    private final String world;
    private final int chunkX;
    private final int chunkZ;
    private final UUID owner;
    private String ownerName;
    private final Set<UUID> trustedUsers;
    private final long createdAt;
    private boolean pvp;
    private boolean explosions;

    public Claim(String world, int chunkX, int chunkZ, UUID owner, String ownerName, Set<UUID> trustedUsers, long createdAt, boolean pvp, boolean explosions) {
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.owner = owner;
        this.ownerName = ownerName;
        this.trustedUsers = new HashSet<>(trustedUsers != null ? trustedUsers : Collections.emptySet());
        this.createdAt = createdAt;
        this.pvp = pvp;
        this.explosions = explosions;
    }

    public String getWorld() {
        return world;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Set<UUID> getTrustedUsers() {
        return Collections.unmodifiableSet(trustedUsers);
    }

    public boolean isTrusted(UUID uuid) {
        return owner.equals(uuid) || trustedUsers.contains(uuid);
    }

    public boolean addTrusted(UUID uuid) {
        return trustedUsers.add(uuid);
    }

    public boolean removeTrusted(UUID uuid) {
        return trustedUsers.remove(uuid);
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isPvp() {
        return pvp;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }

    public boolean isExplosions() {
        return explosions;
    }

    public void setExplosions(boolean explosions) {
        this.explosions = explosions;
    }

    public String getKey() {
        return world + ";" + chunkX + ";" + chunkZ;
    }
}
