package dev.nexoria.announcer;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AnnouncerCommand implements BasicCommand, CommandExecutor, TabCompleter {

    private final NexoriaAnnouncerPlugin plugin;
    private final boolean isBroadcastOnly;

    public AnnouncerCommand(NexoriaAnnouncerPlugin plugin, boolean isBroadcastOnly) {
        this.plugin = plugin;
        this.isBroadcastOnly = isBroadcastOnly;
    }

    private String msg(String key) {
        String p = plugin.getConfig().getString("messages.prefix", "&8[&6Broadcast&8] ");
        String m = plugin.getConfig().getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', p + m);
    }

    private void handleCommand(CommandSender sender, String[] args) {
        if (isBroadcastOnly) {
            handleBroadcast(sender, args);
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("nexoria.announcer.admin")) {
                sender.sendMessage(msg("no-permission"));
                return;
            }
            plugin.reloadConfig();
            plugin.getAnnouncerManager().reload();
            sender.sendMessage(msg("reloaded"));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("next")) {
            if (!sender.hasPermission("nexoria.announcer.admin")) {
                sender.sendMessage(msg("no-permission"));
                return;
            }
            plugin.getAnnouncerManager().broadcastNext();
            sender.sendMessage(ChatColor.GREEN + "Triggered next scheduled announcement.");
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("nexoria.announcer.admin")) {
                sender.sendMessage(msg("no-permission"));
                return;
            }
            var list = plugin.getAnnouncerManager().getAnnouncements();
            sender.sendMessage(ChatColor.GOLD + "Registered announcements (" + list.size() + "):");
            for (Announcement ann : list) {
                sender.sendMessage(ChatColor.YELLOW + "- " + ann.id() + " (" + (ann.chatLines() != null ? ann.chatLines().size() : 0) + " lines)");
            }
            return;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6NexoriaAnnouncer &7v" + plugin.getPluginMeta().getVersion()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/announcer reload &7- Reload config & announcements"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/announcer next   &7- Broadcast next announcement"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/announcer list   &7- List all announcements"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/broadcast <text> &7- Send custom alert to server"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
    }

    private void handleBroadcast(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexoria.announcer.broadcast")) {
            sender.sendMessage(msg("no-permission"));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /broadcast <message>");
            return;
        }

        String message = String.join(" ", args);
        plugin.getAnnouncerManager().manualBroadcast(message);
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
        if (!isBroadcastOnly && args.length == 1 && sender.hasPermission("nexoria.announcer.admin")) {
            list.add("reload");
            list.add("next");
            list.add("list");
        }
        return list;
    }
}
