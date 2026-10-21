package dev.nexoria.scoreboard;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScoreboardCommand implements BasicCommand, CommandExecutor, TabCompleter {

    private final NexoriaScoreboardPlugin plugin;

    public ScoreboardCommand(NexoriaScoreboardPlugin plugin) {
        this.plugin = plugin;
    }

    private String msg(String key) {
        String p = plugin.getConfig().getString("messages.prefix", "&8[&bScoreboard&8] ");
        String m = plugin.getConfig().getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', p + m);
    }

    private void handleCommand(CommandSender sender, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("nexoria.scoreboard.admin")) {
                sender.sendMessage(msg("no-permission"));
                return;
            }
            plugin.reloadConfig();
            sender.sendMessage(msg("reloaded"));
            return;
        }

        if (args.length > 0 && (args[0].equalsIgnoreCase("toggle") || args[0].equalsIgnoreCase("t"))) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Only players can toggle their scoreboard.");
                return;
            }
            if (!player.hasPermission("nexoria.scoreboard.use")) {
                player.sendMessage(msg("no-permission"));
                return;
            }
            boolean visible = plugin.getScoreboardManager().toggleScoreboard(player);
            player.sendMessage(visible ? msg("toggled-on") : msg("toggled-off"));
            return;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bNexoriaScoreboard &7v" + plugin.getPluginMeta().getVersion()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/sb toggle &7- Toggle scoreboard visibility"));
        if (sender.hasPermission("nexoria.scoreboard.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/sb reload &7- Reload configuration"));
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        handleCommand(source.getSender(), args);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return getCompletions(source.getSender(), args);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        handleCommand(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return getCompletions(sender, args);
    }

    private List<String> getCompletions(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("nexoria.scoreboard.use")) list.add("toggle");
            if (sender.hasPermission("nexoria.scoreboard.admin")) list.add("reload");
        }
        return list;
    }
}
