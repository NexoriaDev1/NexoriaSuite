package dev.nexoria.claims;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class ClaimsListener implements Listener {

    private final NexoriaClaimsPlugin plugin;

    public ClaimsListener(NexoriaClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    private void notifyDenied(Player player, Claim claim) {
        String msg = plugin.getConfig().getString("messages.claim-protected", "&cThis area is claimed by &e{owner}&c.");
        msg = msg.replace("{owner}", claim.getOwnerName());
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Claim claim = plugin.getClaimManager().getClaim(event.getBlock().getLocation());
        if (claim != null && !plugin.getClaimManager().canAccess(player, event.getBlock().getLocation())) {
            event.setCancelled(true);
            notifyDenied(player, claim);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Claim claim = plugin.getClaimManager().getClaim(event.getBlock().getLocation());
        if (claim != null && !plugin.getClaimManager().canAccess(player, event.getBlock().getLocation())) {
            event.setCancelled(true);
            notifyDenied(player, claim);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.PHYSICAL) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        Claim claim = plugin.getClaimManager().getClaim(block.getLocation());
        if (claim == null || plugin.getClaimManager().canAccess(player, block.getLocation())) {
            return;
        }

        Material type = block.getType();
        String name = type.name();
        if (name.contains("CHEST") || name.contains("BARREL") || name.contains("SHULKER") ||
            name.contains("DOOR") || name.contains("GATE") || name.contains("TRAPDOOR") ||
            name.contains("BUTTON") || name.contains("LEVER") || name.contains("ANVIL") ||
            name.contains("FURNACE") || name.contains("HOPPER") || name.contains("DROPPER") ||
            name.contains("DISPENSER") || event.getAction() == Action.PHYSICAL) {
            event.setCancelled(true);
            notifyDenied(player, claim);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) {
            return;
        }

        Claim claim = plugin.getClaimManager().getClaim(event.getEntity().getLocation());
        if (claim == null) return;

        if (event.getEntity() instanceof Player victim) {
            if (!claim.isPvp() && !damager.hasPermission("nexoria.claims.admin.bypass")) {
                event.setCancelled(true);
                damager.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.pvp-disabled", "&cPvP is disabled in this claim.")));
            }
            return;
        }

        if (!(event.getEntity() instanceof Monster)) {
            if (!plugin.getClaimManager().canAccess(damager, event.getEntity().getLocation())) {
                event.setCancelled(true);
                notifyDenied(damager, claim);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> {
            Claim claim = plugin.getClaimManager().getClaim(block.getLocation());
            return claim != null && !claim.isExplosions();
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> {
            Claim claim = plugin.getClaimManager().getClaim(block.getLocation());
            return claim != null && !claim.isExplosions();
        });
    }
}
