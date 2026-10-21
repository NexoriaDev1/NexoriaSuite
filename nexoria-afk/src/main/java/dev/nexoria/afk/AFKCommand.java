package dev.nexoria.afk;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Dual Paper-Brigadier and Bukkit command implementation for NexoriaAFK.
 * Handles /afk toggle, status, and reload.
 */
public final class AFKCommand extends Command implements BasicCommand {

    private final NexoriaAFKPlugin plugin;

    public AFKCommand(NexoriaAFKPlugin plugin) {
        super("afk", "Toggle your AFK status or inspect server idle players", "/afk [status|reload|help]", List.of("away"));
        this.plugin = plugin;
        setPermission("nexoria.afk.use");
    }

    // --- Paper BasicCommand Implementation ---

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        handleExecution(stack.getSender(), args);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        return getSuggestions(args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("nexoria.afk.use");
    }

    @Override
    public String permission() {
        return "nexoria.afk.use";
    }

    // --- Bukkit Command Fallback Implementation ---

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return handleExecution(sender, args);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return getSuggestions(args);
    }

    // --- Unified Command Logic ---

    private boolean handleExecution(CommandSender sender, String[] args) {
        AFKAPI api = plugin.getApi();
        if (api == null) {
            sender.sendMessage("§e[NexoriaAFK] AFK service is initializing...");
            return true;
        }

        // Toggle /afk for player with no args
        if (args.length == 0) {
            if (sender instanceof Player p) {
                boolean currentlyAfk = api.isAFK(p.getUniqueId());
                api.setAFK(p, !currentlyAfk, true);
            } else {
                sender.sendMessage("§8[§6NexoriaAFK§8] §cConsole cannot be AFK. Use: §f/afk status");
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "help" -> {
                sender.sendMessage("§8================ §6NexoriaAFK Help §8================");
                sender.sendMessage("§7• §6/afk §8- §7Toggle your idle/AFK status.");
                sender.sendMessage("§7• §6/afk status §8- §7View server AFK metrics and idle players.");
                if (sender.hasPermission("nexoria.afk.admin")) {
                    sender.sendMessage("§7• §6/afk reload §8- §7Reload plugin configuration.");
                }
                sender.sendMessage("§8=====================================================");
            }
            case "status" -> {
                var status = api.getStatus();
                sender.sendMessage("§8================ §6NexoriaAFK Status §8================");
                sender.sendMessage("§7• Operational State: " + (status.active() ? "§aHealthy & Active" : "§cSuspended"));
                sender.sendMessage("§7• Online Players: §f" + status.totalOnline());
                sender.sendMessage("§7• AFK Players: §e" + status.afkCount());
                if (status.afkCount() > 0) {
                    StringBuilder sb = new StringBuilder("§7Idle: ");
                    for (UUID id : api.getAfkPlayers()) {
                        Player p = Bukkit.getPlayer(id);
                        if (p != null) {
                            sb.append("§e").append(p.getName()).append("§7, ");
                        }
                    }
                    sender.sendMessage(sb.toString().replaceAll(", $", ""));
                }
                sender.sendMessage("§8========================================================");
            }
            case "reload" -> {
                if (!sender.hasPermission("nexoria.afk.admin")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.no-permission", "&cYou lack permission: nexoria.afk.admin")));
                    return true;
                }
                plugin.reloadConfig();
                api.reload();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.reloaded", "&a[NexoriaAFK] Configuration successfully reloaded.")));
            }
            default -> sender.sendMessage("§cUnknown subcommand. Use §f/afk help");
        }

        return true;
    }

    private List<String> getSuggestions(String[] args) {
        if (args.length <= 1) {
            return List.of("status", "reload", "help");
        }
        return List.of();
    }
}
