package dev.nexoria.combat;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CombatCommand implements BasicCommand, CommandExecutor, TabCompleter {

    private final NexoriaCombatPlugin plugin;

    public CombatCommand(NexoriaCombatPlugin plugin) {
        this.plugin = plugin;
    }

    private String msg(String key) {
        String p = plugin.getConfig().getString("messages.prefix", "&8[&cCombat&8] ");
        String m = plugin.getConfig().getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', p + m);
    }

    private void handleCommand(CommandSender sender, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("nexoria.combat.admin")) {
                sender.sendMessage(msg("no-permission"));
                return;
            }
            plugin.reloadConfig();
            sender.sendMessage(msg("reloaded"));
            return;
        }

        if (args.length > 1 && args[0].equalsIgnoreCase("untag")) {
            if (!sender.hasPermission("nexoria.combat.admin")) {
                sender.sendMessage(msg("no-permission"));
                return;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                return;
            }
            plugin.getCombatManager().untag(target.getUniqueId());
            String text = plugin.getConfig().getString("messages.untagged-player", "&aRemoved {player} from combat.").replace("{player}", target.getName());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&cCombat&8] ") + text));
            return;
        }

        if (sender instanceof Player player) {
            if (plugin.getCombatManager().isTagged(player.getUniqueId())) {
                int sec = plugin.getCombatManager().getRemainingSeconds(player.getUniqueId());
                String format = plugin.getConfig().getString("messages.status-tagged", "&cYou are currently in combat for &e{time}s&c.");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&cCombat&8] ") + format.replace("{time}", String.valueOf(sec))));
            } else {
                player.sendMessage(msg("status-safe"));
            }
            return;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNexoriaCombat &7v" + plugin.getPluginMeta().getVersion()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/combat untag <player> &7- Remove player from combat"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/combat reload         &7- Reload configuration"));
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
        if (args.length == 1 && sender.hasPermission("nexoria.combat.admin")) {
            list.add("untag");
            list.add("reload");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("untag") && sender.hasPermission("nexoria.combat.admin")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                list.add(p.getName());
            }
        }
        return list;
    }
}
