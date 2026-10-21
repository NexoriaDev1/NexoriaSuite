package dev.nexoria.scoreboard;

import org.bukkit.entity.Player;

/**
 * Public developer API for NexoriaScoreboard.
 */
public class ScoreboardAPI {

    private final NexoriaScoreboardPlugin plugin;

    public ScoreboardAPI(NexoriaScoreboardPlugin plugin) {
        this.plugin = plugin;
    }

    public void updatePlayer(Player player) {
        plugin.getScoreboardManager().updatePlayer(player);
    }

    public boolean toggleScoreboard(Player player) {
        return plugin.getScoreboardManager().toggleScoreboard(player);
    }

    public ScoreboardManager getManager() {
        return plugin.getScoreboardManager();
    }
}
