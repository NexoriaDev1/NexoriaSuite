package dev.nexoria.customitems;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class CustomItemsListener implements Listener {

    private final NexoriaCustomItemsPlugin plugin;

    public CustomItemsListener(NexoriaCustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        CustomItem customItem = plugin.getItemManager().getCustomItem(item);
        if (customItem == null) return;

        String ability = customItem.getAbility();
        if (ability.equals("NONE") || ability.isEmpty()) return;

        if (plugin.getItemManager().isOnCooldown(player.getUniqueId(), customItem.getId())) {
            long rem = plugin.getItemManager().getCooldownRemainingSeconds(player.getUniqueId(), customItem.getId());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&8[&6Items&8] &cItem on cooldown: &e" + rem + "s"));
            event.setCancelled(true);
            return;
        }

        boolean executed = false;

        switch (ability) {
            case "TELEPORT" -> {
                Vector direction = player.getLocation().getDirection().normalize().multiply(8);
                Location target = player.getLocation().add(direction);
                if (target.getBlock().isPassable()) {
                    player.teleport(target);
                    player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                    executed = true;
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cDestination is obstructed."));
                }
            }
            case "HEAL" -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 300, 0));
                player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                executed = true;
            }
            case "DASH" -> {
                Vector dir = player.getLocation().getDirection().setY(0.2).normalize().multiply(1.5);
                player.setVelocity(dir);
                player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1f, 1.2f);
                executed = true;
            }
        }

        if (executed) {
            event.setCancelled(true);
            plugin.getItemManager().setCooldown(player.getUniqueId(), customItem.getId(), customItem.getCooldownSeconds());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        CustomItem customItem = plugin.getItemManager().getCustomItem(item);
        if (customItem == null) return;

        if (customItem.getAbility().equalsIgnoreCase("LIGHTNING")) {
            if (plugin.getItemManager().isOnCooldown(player.getUniqueId(), customItem.getId())) {
                return;
            }
            if (event.getEntity() instanceof LivingEntity target) {
                target.getWorld().strikeLightningEffect(target.getLocation());
                target.damage(3.0);
                plugin.getItemManager().setCooldown(player.getUniqueId(), customItem.getId(), customItem.getCooldownSeconds());
            }
        }
    }
}
