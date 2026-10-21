package dev.nexoria.skills;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;

public final class SkillsListener implements Listener {

    private final NexoriaSkillsPlugin plugin;

    public SkillsListener(NexoriaSkillsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Material mat = event.getBlock().getType();
        String name = mat.name();

        if (name.contains("LOG") || name.contains("WOOD")) {
            plugin.getSkillManager().awardExp(player, SkillType.WOODCUTTING, 10.0);
        } else if (name.contains("ORE") || name.equals("ANCIENT_DEBRIS")) {
            double xp = name.contains("DIAMOND") || name.contains("EMERALD") || name.contains("DEBRIS") ? 35.0 : 15.0;
            plugin.getSkillManager().awardExp(player, SkillType.MINING, xp);
        } else if (name.equals("STONE") || name.equals("DEEPSLATE") || name.equals("NETHERRACK") || name.equals("END_STONE")) {
            plugin.getSkillManager().awardExp(player, SkillType.MINING, 1.0);
        } else if (name.equals("WHEAT") || name.equals("CARROTS") || name.equals("POTATOES") || name.equals("BEETROOTS") || name.equals("NETHER_WART")) {
            plugin.getSkillManager().awardExp(player, SkillType.FARMING, 12.0);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        if (event.getEntity() instanceof Monster) {
            plugin.getSkillManager().awardExp(killer, SkillType.COMBAT, 20.0);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            plugin.getSkillManager().awardExp(event.getPlayer(), SkillType.FISHING, 25.0);
        }
    }
}
