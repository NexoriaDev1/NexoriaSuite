package dev.nexoria.quests;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public final class QuestsListener implements Listener {

    private final NexoriaQuestsPlugin plugin;

    public QuestsListener(NexoriaQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        plugin.getQuestManager().handleProgress(killer, Quest.QuestType.KILL_MOB, event.getEntityType().name(), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        String matName = event.getBlock().getType().name();
        plugin.getQuestManager().handleProgress(player, Quest.QuestType.MINE_BLOCK, matName, 1);
    }
}
