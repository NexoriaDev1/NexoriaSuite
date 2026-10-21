package dev.nexoria.sync;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SyncCommand extends Command implements BasicCommand {

    private final NexoriaSyncPlugin plugin;

    public SyncCommand(NexoriaSyncPlugin plugin) {
        super("sync", "Synchronize player state across network", "/sync [save|load|reload]", List.of("playersync", "psync"));
        this.plugin = plugin;
        setPermission("nexoria.sync.admin");
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        handleExecution(stack.getSender(), args);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        return getSuggestions(args);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return handleExecution(sender, args);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return getSuggestions(args);
    }

    private boolean handleExecution(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexoria.sync.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission", "&cYou lack permission: nexoria.sync.admin")));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§8================ §3NexoriaSync §8================");
            sender.sendMessage("§7• §3/sync save [player] §8- §7Manually snapshot player sync state");
            sender.sendMessage("§7• §3/sync load <player> §8- §7Force-apply player sync state");
            sender.sendMessage("§7• §3/sync reload §8- §7Reload sync storage and config");
            sender.sendMessage("§8================================================");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "save" -> {
                Player target = null;
                if (args.length >= 2) {
                    target = Bukkit.getPlayer(args[1]);
                } else if (sender instanceof Player p) {
                    target = p;
                }
                if (target == null) {
                    sender.sendMessage("§cSpecify a valid online player: /sync save <player>");
                    return true;
                }
                plugin.getSyncManager().savePlayer(target);
                sender.sendMessage("§a[NexoriaSync] Successfully saved sync state for §e" + target.getName() + "§a.");
            }
            case "load" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /sync load <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found: " + args[1]);
                    return true;
                }
                boolean ok = plugin.getSyncManager().applyPlayer(target);
                if (ok) {
                    sender.sendMessage("§a[NexoriaSync] Applied synchronized state to §e" + target.getName() + "§a.");
                } else {
                    sender.sendMessage("§c[NexoriaSync] No synchronized state found for §e" + target.getName() + "§c.");
                }
            }
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getSyncManager().loadData();
                sender.sendMessage("§a[NexoriaSync] Configuration and sync data reloaded.");
            }
            default -> sender.sendMessage("§cUnknown subcommand. Use §f/sync help");
        }
        return true;
    }

    private List<String> getSuggestions(String[] args) {
        if (args.length <= 1) {
            return List.of("save", "load", "reload");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("save") || args[0].equalsIgnoreCase("load"))) {
            List<String> list = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                list.add(p.getName());
            }
            return list;
        }
        return List.of();
    }
}
