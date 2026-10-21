package dev.nexoria.vanish;

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
 * Universal command handler for /vanish, /v.
 */
public final class VanishCommand extends Command implements BasicCommand {

    private final NexoriaVanishPlugin plugin;

    public VanishCommand(NexoriaVanishPlugin plugin) {
        super("vanish", "Toggle stealth vanish mode", "/vanish [status|reload|help]", List.of("v"));
        this.plugin = plugin;
        setPermission("nexoria.vanish.admin");
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
        return sender.hasPermission("nexoria.vanish.admin");
    }

    @Override
    public String permission() {
        return "nexoria.vanish.admin";
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
        VanishAPI api = plugin.getApi();
        if (api == null) {
            sender.sendMessage("§e[NexoriaVanish] Vanish service is still initializing...");
            return true;
        }

        if (args.length > 0) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "help" -> {
                    sender.sendMessage("§8================ §8NexoriaVanish Help §8================");
                    sender.sendMessage("§7• §8/vanish §8- §7Toggle stealth invisibility for yourself.");
                    sender.sendMessage("§7• §8/vanish status §8- §7Show who is currently vanished.");
                    sender.sendMessage("§7• §8/vanish reload §8- §7Reload vanish configuration.");
                    sender.sendMessage("§8========================================================");
                    return true;
                }
                case "status" -> {
                    var status = api.getStatus();
                    sender.sendMessage("§8================ §8NexoriaVanish Status §8================");
                    sender.sendMessage("§7• Operational State: " + (status.active() ? "§aHealthy & Active" : "§cSuspended"));
                    sender.sendMessage("§7• Currently Vanished: §f" + status.totalVanished());
                    if (status.totalVanished() > 0) {
                        StringBuilder sb = new StringBuilder("§7Players: ");
                        for (UUID id : api.getVanishedPlayers()) {
                            Player p = Bukkit.getPlayer(id);
                            if (p != null) {
                                sb.append("§a").append(p.getName()).append("§7, ");
                            }
                        }
                        sender.sendMessage(sb.toString().replaceAll(", $", ""));
                    }
                    sender.sendMessage("§8==========================================================");
                    return true;
                }
                case "reload" -> {
                    plugin.reloadConfig();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.reloaded", "&a[NexoriaVanish] Configuration successfully reloaded.")));
                    return true;
                }
            }
        }

        // Toggle vanish for player
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.only-player", "&cThis command can only be executed by in-game players.")));
            return true;
        }

        boolean current = api.isVanished(player.getUniqueId());
        api.setVanished(player, !current);
        return true;
    }

    private List<String> getSuggestions(String[] args) {
        if (args.length <= 1) {
            return List.of("status", "reload", "help");
        }
        return List.of();
    }
}
