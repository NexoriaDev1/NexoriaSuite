package dev.nexoria.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public final class LootEntry {

    private final String targetId; // mob type or block type
    private final Material material;
    private final double chancePercent;
    private final int minAmount;
    private final int maxAmount;

    public LootEntry(String targetId, Material material, double chancePercent, int minAmount, int maxAmount) {
        this.targetId = targetId.toUpperCase();
        this.material = material != null ? material : Material.DIAMOND;
        this.chancePercent = chancePercent;
        this.minAmount = Math.max(1, minAmount);
        this.maxAmount = Math.max(this.minAmount, maxAmount);
    }

    public String getTargetId() {
        return targetId;
    }

    public Material getMaterial() {
        return material;
    }

    public double getChancePercent() {
        return chancePercent;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public boolean roll() {
        if (chancePercent >= 100.0) return true;
        if (chancePercent <= 0.0) return false;
        double rand = ThreadLocalRandom.current().nextDouble(0.0, 100.0);
        return rand <= chancePercent;
    }

    public ItemStack createDrop() {
        int amount = ThreadLocalRandom.current().nextInt(minAmount, maxAmount + 1);
        return new ItemStack(material, amount);
    }
}
