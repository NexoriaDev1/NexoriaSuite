package dev.nexoria.adventure;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AdventureCommand implements BasicCommand, CommandExecutor, TabCompleter {

    private final NexoriaAdventurePlugin plugin;

    public AdventureCommand(NexoriaAdventurePlugin plugin) {
        this.plugin = plugin;
    }

    private void handleCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexoria.adventure.admin")) {
            Component noPerm = plugin.getAdventureManager().parse(plugin.getConfig().getString("messages.no-permission", "<red>No permission.</red>"));
            sender.sendMessage(noPerm);
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            Component reloaded = plugin.getAdventureManager().parse(plugin.getConfig().getString("messages.reloaded", "<green>Configuration reloaded.</green>"));
            sender.sendMessage(reloaded);
            return;
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("msg")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                return;
            }
            String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            Component parsed = plugin.getAdventureManager().parse(text);
            target.sendMessage(parsed);
            sender.sendMessage(ChatColor.GREEN + "Sent MiniMessage to " + target.getName() + ".");
            return;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("bossbar")) {
            int seconds = 10;
            BossBar.Color color = BossBar.Color.PURPLE;
            try {
                color = BossBar.Color.valueOf(plugin.getConfig().getString("settings.default-bossbar-color", "PURPLE").toUpperCase());
            } catch (Exception ignored) {}

            int textEnd = args.length;
            if (args.length >= 3) {
                try {
                    seconds = Integer.parseInt(args[args.length - 1]);
                    textEnd = args.length - 1;
                } catch (NumberFormatException ignored) {}
            }

            String text = String.join(" ", Arrays.copyOfRange(args, 1, textEnd));
            plugin.getAdventureManager().broadcastBossBar(text, seconds, color);

            Component confirm = plugin.getAdventureManager().parse(
                    plugin.getConfig().getString("messages.bossbar-sent", "<green>Sent boss bar announcement for <yellow>{seconds}s</yellow>.</green>")
                            .replace("{seconds}", String.valueOf(seconds))
            );
            sender.sendMessage(confirm);
            return;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&dNexoriaAdventure &7v" + plugin.getPluginMeta().getVersion()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/adv msg <player> <text>            &7- Send MiniMessage to player"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/adv bossbar <text> [seconds]       &7- Broadcast animated bossbar"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/adv reload                         &7- Reload configuration"));
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
        if (!sender.hasPermission("nexoria.adventure.admin")) return list;

        if (args.length == 1) {
            list.add("msg");
            list.add("bossbar");
            list.add("reload");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("msg")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                list.add(p.getName());
            }
        }
        return list;
    }
}
