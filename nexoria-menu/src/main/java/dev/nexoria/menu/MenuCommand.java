package dev.nexoria.menu;

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

public class MenuCommand implements BasicCommand, CommandExecutor, TabCompleter {

    private final NexoriaMenuPlugin plugin;

    public MenuCommand(NexoriaMenuPlugin plugin) {
        this.plugin = plugin;
    }

    private String msg(String key) {
        String p = plugin.getConfig().getString("messages.prefix", "&8[&bMenu&8] ");
        String m = plugin.getConfig().getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', p + m);
    }

    private void handleCommand(CommandSender sender, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("nexoria.menu.admin")) {
                sender.sendMessage(msg("no-permission"));
                return;
            }
            plugin.reloadConfig();
            plugin.getMenuManager().reload();
            sender.sendMessage(msg("reloaded"));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only in-game players can open the menu GUI.");
            return;
        }

        if (!player.hasPermission("nexoria.menu.use")) {
            player.sendMessage(msg("no-permission"));
            return;
        }

        plugin.getMenuManager().openMainMenu(player);
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
        if (args.length == 1 && sender.hasPermission("nexoria.menu.admin")) {
            list.add("reload");
        }
        return list;
    }
}
