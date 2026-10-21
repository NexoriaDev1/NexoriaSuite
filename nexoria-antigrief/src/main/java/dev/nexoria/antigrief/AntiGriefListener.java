package dev.nexoria.antigrief;

import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class AntiGriefListener implements Listener {

    private final NexoriaAntiGriefPlugin plugin;

    public AntiGriefListener(NexoriaAntiGriefPlugin plugin) {
        this.plugin = plugin;
    }

    private AntiGriefManager mgr() {
        return plugin.getAntiGriefManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Creeper && mgr().isProtected("block-creeper-damage")) {
            event.blockList().clear();
        } else if ((entity instanceof TNTPrimed || entity instanceof Minecart) && mgr().isProtected("block-tnt-damage")) {
            event.blockList().clear();
        } else if ((entity instanceof Wither || entity instanceof WitherSkull) && mgr().isProtected("block-wither-damage")) {
            event.blockList().clear();
        } else if (entity instanceof EnderCrystal && mgr().isProtected("block-crystal-damage")) {
            event.blockList().clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (mgr().isProtected("block-bed-anchor-damage")) {
            Material type = event.getBlock().getType();
            if (type.name().endsWith("_BED") || type == Material.RESPAWN_ANCHOR) {
                event.blockList().clear();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        if (event.getSource().getType() == Material.FIRE && mgr().isProtected("block-fire-spread")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (mgr().isProtected("block-fire-burn")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTrample(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == Material.FARMLAND && mgr().isProtected("block-crop-trample")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTrample(EntityInteractEvent event) {
        if (event.getBlock().getType() == Material.FARMLAND && mgr().isProtected("block-crop-trample")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Enderman && mgr().isProtected("block-enderman-grief")) {
            event.setCancelled(true);
        } else if (event.getEntity() instanceof Ravager && mgr().isProtected("block-ravager-grief")) {
            event.setCancelled(true);
        }
    }
}
