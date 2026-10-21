package dev.nexoria.antigrief;

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
import java.util.Map;

public class AntiGriefCommand implements BasicCommand, CommandExecutor, TabCompleter {

    private final NexoriaAntiGriefPlugin plugin;

    public AntiGriefCommand(NexoriaAntiGriefPlugin plugin) {
        this.plugin = plugin;
    }

    private String msg(String key) {
        String p = plugin.getConfig().getString("messages.prefix", "&8[&9AntiGrief&8] ");
        String m = plugin.getConfig().getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', p + m);
    }

    private void handleCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexoria.antigrief.admin")) {
            sender.sendMessage(msg("no-permission"));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getAntiGriefManager().reload();
            sender.sendMessage(msg("reloaded"));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("status")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.status-header", "&9AntiGrief Active Protections:")));
            for (Map.Entry<String, Boolean> entry : plugin.getAntiGriefManager().getProtections().entrySet()) {
                String color = entry.getValue() ? "&a[ENABLED]" : "&c[DISABLED]";
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7- " + entry.getKey() + ": " + color));
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
            return;
        }

        if (args.length > 1 && args[0].equalsIgnoreCase("toggle")) {
            String feature = args[1].toLowerCase();
            if (!plugin.getAntiGriefManager().getProtections().containsKey(feature)) {
                sender.sendMessage(ChatColor.RED + "Unknown protection feature: " + feature);
                return;
            }

            boolean active = plugin.getAntiGriefManager().toggleProtection(feature);
            String status = active ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED";
            String text = plugin.getConfig().getString("messages.toggle-status", "&7Protection &e{feature} &7is now &f{status}&7.")
                    .replace("{feature}", feature)
                    .replace("{status}", status);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&9AntiGrief&8] ") + text));
            return;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9NexoriaAntiGrief &7v" + plugin.getPluginMeta().getVersion()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/antigrief status           &7- View active protections"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/antigrief toggle <feature> &7- Toggle protection realtime"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/antigrief reload           &7- Reload configuration"));
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
        if (!sender.hasPermission("nexoria.antigrief.admin")) return list;

        if (args.length == 1) {
            list.add("status");
            list.add("toggle");
            list.add("reload");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            list.addAll(plugin.getAntiGriefManager().getProtections().keySet());
        }
        return list;
    }
}
