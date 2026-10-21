package dev.nexoria.spawners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class SpawnersListener implements Listener {

    private final NexoriaSpawnersPlugin plugin;

    public SpawnersListener(NexoriaSpawnersPlugin plugin) {
        this.plugin = plugin;
    }

    private String msg(String key) {
        String p = plugin.getConfig().getString("messages.prefix", "&8[&dSpawners&8] ");
        String m = plugin.getConfig().getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', p + m);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.SPAWNER) {
            return;
        }

        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (plugin.getConfig().getBoolean("settings.require-silk-touch", true)) {
            ItemStack tool = player.getInventory().getItemInMainHand();
            int requiredLevel = plugin.getConfig().getInt("settings.silk-touch-level", 1);
            if (tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) < requiredLevel) {
                return;
            }
        }

        if (!player.hasPermission("nexoria.spawners.mine")) {
            player.sendMessage(msg("no-mine-permission"));
            event.setCancelled(true);
            return;
        }

        int dropChance = plugin.getConfig().getInt("settings.drop-chance", 100);
        if (ThreadLocalRandom.current().nextInt(100) >= dropChance) {
            return;
        }

        EntityType type = plugin.getSpawnerManager().getBlockSpawnerType(block);
        ItemStack spawnerItem = plugin.getSpawnerManager().createSpawnerItem(type, 1);

        event.setExpToDrop(0);
        event.setDropItems(false);
        block.getWorld().dropItemNaturally(block.getLocation(), spawnerItem);

        if (plugin.getConfig().getBoolean("settings.effects", true)) {
            block.getWorld().spawnParticle(Particle.PORTAL, block.getLocation().add(0.5, 0.5, 0.5), 25, 0.3, 0.3, 0.3, 0.1);
            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_ANVIL_USE, 0.8f, 1.2f);
        }

        String mobName = plugin.getSpawnerManager().formatName(type.name());
        player.sendMessage(msg("mined").replace("{type}", mobName));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        if (block.getType() != Material.SPAWNER) {
            return;
        }

        ItemStack item = event.getItemInHand();
        EntityType type = plugin.getSpawnerManager().getSpawnerType(item);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getSpawnerManager().setBlockSpawnerType(block, type);
        }, 1L);

        if (plugin.getConfig().getBoolean("settings.effects", true)) {
            block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(0.5, 0.5, 0.5), 15, 0.3, 0.3, 0.3, 0.05);
            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
        }

        String mobName = plugin.getSpawnerManager().formatName(type.name());
        event.getPlayer().sendMessage(msg("placed").replace("{type}", mobName));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block.getType() != Material.SPAWNER) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !item.getType().name().endsWith("_SPAWN_EGG")) {
            return;
        }

        if (!plugin.getConfig().getBoolean("settings.allow-spawn-egg-change", true)) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("nexoria.spawners.change")) {
            event.setCancelled(true);
            player.sendMessage(msg("no-egg-permission"));
            return;
        }

        String eggType = item.getType().name().replace("_SPAWN_EGG", "");
        try {
            EntityType newType = EntityType.valueOf(eggType);
            event.setCancelled(true);

            if (plugin.getConfig().getBoolean("settings.consume-spawn-egg", true) && player.getGameMode() != GameMode.CREATIVE) {
                item.setAmount(item.getAmount() - 1);
            }

            plugin.getSpawnerManager().setBlockSpawnerType(block, newType);

            if (plugin.getConfig().getBoolean("settings.effects", true)) {
                block.getWorld().spawnParticle(Particle.PORTAL, block.getLocation().add(0.5, 0.5, 0.5), 20, 0.2, 0.2, 0.2, 0.1);
                block.getWorld().playSound(block.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
            }

            String mobName = plugin.getSpawnerManager().formatName(newType.name());
            player.sendMessage(msg("changed").replace("{type}", mobName));
        } catch (Exception ignored) {}
    }
}
