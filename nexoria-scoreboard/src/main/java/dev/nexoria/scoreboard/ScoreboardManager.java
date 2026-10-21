package dev.nexoria.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager {

    private final NexoriaScoreboardPlugin plugin;
    private final Set<UUID> disabledPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, FastBoardHolder> activeBoards = new ConcurrentHashMap<>();
    private Economy economy;
    private int titleFrameIndex = 0;

    public ScoreboardManager(NexoriaScoreboardPlugin plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.economy = rsp.getProvider();
            }
        }
    }

    public void createScoreboard(Player player) {
        if (disabledPlayers.contains(player.getUniqueId())) {
            return;
        }

        List<String> disabledWorlds = plugin.getConfig().getStringList("settings.disabled-worlds");
        if (disabledWorlds.contains(player.getWorld().getName())) {
            removeScoreboard(player);
            return;
        }

        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = sb.registerNewObjective("nexoria_sb", Criteria.DUMMY, Component.empty());
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        FastBoardHolder holder = new FastBoardHolder(player, sb, obj);
        activeBoards.put(player.getUniqueId(), holder);
        player.setScoreboard(sb);

        updatePlayer(player);
    }

    public void removeScoreboard(Player player) {
        activeBoards.remove(player.getUniqueId());
        if (player.isOnline()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public boolean toggleScoreboard(Player player) {
        UUID uuid = player.getUniqueId();
        if (disabledPlayers.contains(uuid)) {
            disabledPlayers.remove(uuid);
            createScoreboard(player);
            return true;
        } else {
            disabledPlayers.add(uuid);
            removeScoreboard(player);
            return false;
        }
    }

    public void updateAll() {
        titleFrameIndex++;
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    public void updatePlayer(Player player) {
        FastBoardHolder holder = activeBoards.get(player.getUniqueId());
        if (holder == null || !player.isOnline()) {
            return;
        }

        List<String> disabledWorlds = plugin.getConfig().getStringList("settings.disabled-worlds");
        if (disabledWorlds.contains(player.getWorld().getName())) {
            removeScoreboard(player);
            return;
        }

        // Title update
        List<String> frames = plugin.getConfig().getStringList("title.frames");
        if (!frames.isEmpty()) {
            String titleRaw = frames.get(titleFrameIndex % frames.size());
            holder.objective.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(titleRaw));
        }

        // Lines update
        List<String> configLines = plugin.getConfig().getStringList("lines");
        int totalLines = Math.min(configLines.size(), 15);

        for (int i = 0; i < totalLines; i++) {
            String rawLine = configLines.get(i);
            String formatted = replacePlaceholders(player, rawLine);
            holder.setLine(i, totalLines - i, formatted);
        }

        // Clean excess lines if config shrank
        holder.cleanLinesAbove(totalLines);
    }

    private String replacePlaceholders(Player player, String text) {
        if (text == null) return "";
        String balStr = "0.00";
        if (economy != null) {
            try {
                balStr = String.format(Locale.US, "%.2f", economy.getBalance(player));
            } catch (Exception ignored) {}
        }

        return text
                .replace("%player%", player.getName())
                .replace("%displayname%", player.getDisplayName())
                .replace("%ping%", String.valueOf(player.getPing()))
                .replace("%health%", String.valueOf((int) player.getHealth()))
                .replace("%max_health%", String.valueOf((int) player.getMaxHealth()))
                .replace("%balance%", balStr)
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%max_players%", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("%world%", player.getWorld().getName());
    }

    public void shutdown() {
        for (UUID uuid : activeBoards.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
        activeBoards.clear();
        disabledPlayers.clear();
    }

    private static class FastBoardHolder {
        final Player player;
        final Scoreboard scoreboard;
        final Objective objective;
        final Map<Integer, Team> lineTeams = new HashMap<>();

        FastBoardHolder(Player player, Scoreboard scoreboard, Objective objective) {
            this.player = player;
            this.scoreboard = scoreboard;
            this.objective = objective;
        }

        void setLine(int lineIndex, int score, String text) {
            String entry = ChatColor.COLOR_CHAR + "" + Integer.toHexString(lineIndex) + ChatColor.RESET;
            Team team = lineTeams.get(lineIndex);
            if (team == null) {
                String teamName = "nex_line_" + lineIndex;
                team = scoreboard.getTeam(teamName);
                if (team == null) {
                    team = scoreboard.registerNewTeam(teamName);
                }
                if (!team.hasEntry(entry)) {
                    team.addEntry(entry);
                }
                lineTeams.put(lineIndex, team);
            }

            team.prefix(LegacyComponentSerializer.legacyAmpersand().deserialize(text));
            objective.getScore(entry).setScore(score);
        }

        void cleanLinesAbove(int maxLines) {
            Iterator<Map.Entry<Integer, Team>> it = lineTeams.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, Team> entry = it.next();
                if (entry.getKey() >= maxLines) {
                    String colorEntry = ChatColor.COLOR_CHAR + "" + Integer.toHexString(entry.getKey()) + ChatColor.RESET;
                    scoreboard.resetScores(colorEntry);
                    entry.getValue().unregister();
                    it.remove();
                }
            }
        }
    }
}
