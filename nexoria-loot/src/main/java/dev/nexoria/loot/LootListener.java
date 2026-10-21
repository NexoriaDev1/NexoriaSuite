package dev.nexoria.loot;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class LootListener implements Listener {

    private final NexoriaLootPlugin plugin;

    public LootListener(NexoriaLootPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getType() == EntityType.PLAYER) return;

        List<LootEntry> entries = plugin.getLootManager().getEntityDrops(entity.getType().name());
        for (LootEntry entry : entries) {
            if (entry.roll()) {
                ItemStack drop = entry.createDrop();
                event.getDrops().add(drop);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Material blockType = event.getBlock().getType();
        List<LootEntry> entries = plugin.getLootManager().getBlockDrops(blockType.name());
        if (entries.isEmpty()) return;

        Location loc = event.getBlock().getLocation().add(0.5, 0.5, 0.5);
        for (LootEntry entry : entries) {
            if (entry.roll()) {
                ItemStack drop = entry.createDrop();
                loc.getWorld().dropItemNaturally(loc, drop);
            }
        }
    }
}
