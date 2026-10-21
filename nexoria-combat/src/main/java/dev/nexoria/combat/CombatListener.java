package dev.nexoria.combat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class CombatListener implements Listener {

    private final NexoriaCombatPlugin plugin;

    public CombatListener(NexoriaCombatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPvPDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player attacker = null;
        if (event.getDamager() instanceof Player directAttacker) {
            attacker = directAttacker;
        } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            attacker = shooter;
        }

        if (attacker != null && !attacker.getUniqueId().equals(victim.getUniqueId())) {
            plugin.getCombatManager().tag(victim, attacker);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("nexoria.combat.bypass")) {
            return;
        }

        if (plugin.getCombatManager().isTagged(player.getUniqueId())) {
            String message = event.getMessage().toLowerCase();
            String rootCommand = message.split(" ")[0];

            List<String> blocked = plugin.getConfig().getStringList("blocked-commands");
            for (String cmd : blocked) {
                if (rootCommand.equalsIgnoreCase(cmd) || rootCommand.startsWith(cmd.toLowerCase() + " ")) {
                    event.setCancelled(true);
                    String prefix = plugin.getConfig().getString("messages.prefix", "&8[&cCombat&8] ");
                    String format = plugin.getConfig().getString("messages.command-blocked", "&cYou cannot execute &e{command} &cwhile in combat!");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + format.replace("{command}", rootCommand)));
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnderpearl(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() != null && event.getItem().getType() == Material.ENDER_PEARL) {
                if (!plugin.getCombatManager().checkPearlCooldown(event.getPlayer())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGlide(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player player && event.isGliding()) {
            if (plugin.getConfig().getBoolean("settings.block-elytra", true) && plugin.getCombatManager().isTagged(player.getUniqueId())) {
                if (!player.hasPermission("nexoria.combat.bypass")) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You cannot activate Elytra while in combat!");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getCombatManager().handleLogout(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        plugin.getCombatManager().untag(event.getEntity().getUniqueId());
    }
}
