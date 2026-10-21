package dev.nexoria.cooldowns;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CooldownsCommand implements BasicCommand, CommandExecutor, TabCompleter {

    private final NexoriaCooldownsPlugin plugin;

    public CooldownsCommand(NexoriaCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    private String msg(String key) {
        String p = plugin.getConfig().getString("messages.prefix", "&8[&3Cooldowns&8] ");
        String m = plugin.getConfig().getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', p + m);
    }

    private void handleCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexoria.cooldowns.admin")) {
            sender.sendMessage(msg("no-permission"));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getCooldownManager().saveData();
            plugin.getCooldownManager().loadData();
            sender.sendMessage(msg("reloaded"));
            return;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("check")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            Map<String, Long> active = plugin.getCooldownManager().getActiveCooldowns(target.getUniqueId());
            if (active.isEmpty()) {
                sender.sendMessage(msg("no-active-cooldowns").replace("{player}", args[1]));
                return;
            }

            sender.sendMessage(ChatColor.GOLD + "Active cooldowns for " + args[1] + ":");
            for (Map.Entry<String, Long> entry : active.entrySet()) {
                String formatted = plugin.getCooldownManager().formatTime(entry.getValue());
                sender.sendMessage(ChatColor.YELLOW + "- " + entry.getKey() + ": " + ChatColor.WHITE + formatted);
            }
            return;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("reset")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (args.length >= 3 && !args[2].equalsIgnoreCase("all")) {
                plugin.getCooldownManager().resetCooldown(target.getUniqueId(), args[2].toLowerCase());
                sender.sendMessage(ChatColor.GREEN + "Reset cooldown " + args[2] + " for " + args[1] + ".");
            } else {
                plugin.getCooldownManager().resetAll(target.getUniqueId());
                sender.sendMessage(msg("cooldown-reset").replace("{player}", args[1]));
            }
            return;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3NexoriaCooldowns &7v" + plugin.getPluginMeta().getVersion()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/cd check <player>         &7- Inspect active cooldowns"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/cd reset <player> [key]   &7- Reset cooldowns"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/cd reload                 &7- Reload configuration"));
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
        if (!sender.hasPermission("nexoria.cooldowns.admin")) return list;

        if (args.length == 1) {
            list.add("check");
            list.add("reset");
            list.add("reload");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("reset"))) {
            for (var p : Bukkit.getOnlinePlayers()) {
                list.add(p.getName());
            }
        }
        return list;
    }
}
