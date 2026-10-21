package dev.nexoria.combat;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Public developer API for NexoriaCombat.
 */
public class CombatAPI {

    private final NexoriaCombatPlugin plugin;

    public CombatAPI(NexoriaCombatPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isInCombat(UUID uuid) {
        return plugin.getCombatManager().isTagged(uuid);
    }

    public boolean isInCombat(Player player) {
        return isInCombat(player.getUniqueId());
    }

    public int getRemainingCombatTime(UUID uuid) {
        return plugin.getCombatManager().getRemainingSeconds(uuid);
    }

    public void tag(Player victim, Player attacker) {
        plugin.getCombatManager().tag(victim, attacker);
    }

    public void untag(UUID uuid) {
        plugin.getCombatManager().untag(uuid);
    }

    public CombatManager getManager() {
        return plugin.getCombatManager();
    }
}
