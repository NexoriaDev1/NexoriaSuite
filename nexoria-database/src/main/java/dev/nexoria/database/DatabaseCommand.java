package dev.nexoria.database;

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

public class DatabaseCommand implements BasicCommand, CommandExecutor, TabCompleter {

    private final NexoriaDatabasePlugin plugin;

    public DatabaseCommand(NexoriaDatabasePlugin plugin) {
        this.plugin = plugin;
    }

    private String msg(String key) {
        String p = plugin.getConfig().getString("messages.prefix", "&8[&1Database&8] ");
        String m = plugin.getConfig().getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', p + m);
    }

    private void handleCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexoria.database.admin")) {
            sender.sendMessage(msg("no-permission"));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getDatabaseManager().connect();
            sender.sendMessage(msg("reloaded"));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("ping")) {
            boolean active = plugin.getDatabaseManager().isConnected();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.prefix", "&8[&1Database&8] ") +
                            (active ? "&aPing successful: backend is responsive." : "&cPing failed: backend is unreachable.")));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("status")) {
            boolean active = plugin.getDatabaseManager().isConnected();
            String status = active ? ChatColor.GREEN + "ONLINE" : ChatColor.RED + "OFFLINE";
            String text = plugin.getConfig().getString("messages.status", "&7Database backend: &e{type} &7| Status: {status}")
                    .replace("{type}", plugin.getDatabaseManager().getDatabaseType())
                    .replace("{status}", status);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&1Database&8] ") + text));
            return;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&1NexoriaDatabase &7v" + plugin.getPluginMeta().getVersion()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/db status &7- Inspect connection health"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/db ping   &7- Test backend response time"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/db reload &7- Reload config and reconnect"));
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
        if (!sender.hasPermission("nexoria.database.admin")) return list;

        if (args.length == 1) {
            list.add("status");
            list.add("ping");
            list.add("reload");
        }
        return list;
    }
}
