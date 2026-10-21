package dev.nexoria.sync;

import org.bukkit.GameMode;

public final class SyncData {

    private final String inventoryBase64;
    private final String enderChestBase64;
    private final double health;
    private final int foodLevel;
    private final float saturation;
    private final int expLevel;
    private final float expPoints;
    private final GameMode gameMode;
    private final long lastSync;

    public SyncData(String inventoryBase64, String enderChestBase64, double health, int foodLevel,
                    float saturation, int expLevel, float expPoints, GameMode gameMode, long lastSync) {
        this.inventoryBase64 = inventoryBase64 != null ? inventoryBase64 : "";
        this.enderChestBase64 = enderChestBase64 != null ? enderChestBase64 : "";
        this.health = Math.max(1.0, health);
        this.foodLevel = foodLevel;
        this.saturation = saturation;
        this.expLevel = expLevel;
        this.expPoints = expPoints;
        this.gameMode = gameMode != null ? gameMode : GameMode.SURVIVAL;
        this.lastSync = lastSync;
    }

    public String getInventoryBase64() {
        return inventoryBase64;
    }

    public String getEnderChestBase64() {
        return enderChestBase64;
    }

    public double getHealth() {
        return health;
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public float getSaturation() {
        return saturation;
    }

    public int getExpLevel() {
        return expLevel;
    }

    public float getExpPoints() {
        return expPoints;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public long getLastSync() {
        return lastSync;
    }
}
