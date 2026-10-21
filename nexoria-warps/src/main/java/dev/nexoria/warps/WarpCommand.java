package dev.nexoria.warps;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Universal command handler for /warp, /setwarp, /delwarp.
 * Implements Brigadier and Bukkit fallback.
 */
public final class WarpCommand extends Command implements BasicCommand {

    private final NexoriaWarpsPlugin plugin;

    public WarpCommand(NexoriaWarpsPlugin plugin) {
        super("warp", "Teleport to server warps or inspect available destinations", "/warp [name|list|help|status|reload]", List.of("warps"));
        this.plugin = plugin;
        setPermission("nexoria.warps.use");
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
        return sender.hasPermission("nexoria.warps.use");
    }

    @Override
    public String permission() {
        return "nexoria.warps.use";
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

    public boolean handleExecution(CommandSender sender, String[] args) {
        WarpsAPI api = plugin.getApi();
        if (api == null) {
            sender.sendMessage("§e[NexoriaWarps] Warp service is still initializing...");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            var names = api.getWarpNames();
            if (names.isEmpty()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.no-warps", "&7There are currently no warps set.")));
            } else {
                String listStr = String.join("&7, &f", names);
                String msg = plugin.getConfig().getString("messages.warp-list", "&7Available warps (&d{count}&7): &f{warps}")
                        .replace("{count}", String.valueOf(names.size()))
                        .replace("{warps}", listStr);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("help")) {
            sender.sendMessage("§8================ §5NexoriaWarps Help §8================");
            sender.sendMessage("§7• §d/warp <name> §8- §7Teleport to an existing warp.");
            sender.sendMessage("§7• §d/warp list   §8- §7Show all accessible warps.");
            sender.sendMessage("§7• §d/warp status §8- §7Show registered warps count and system health.");
            if (sender.hasPermission("nexoria.warps.admin")) {
                sender.sendMessage("§7• §d/setwarp <name> §8- §7Create a warp at your current location.");
                sender.sendMessage("§7• §d/delwarp <name> §8- §7Delete an existing warp.");
                sender.sendMessage("§7• §d/warp reload   §8- §7Reload warp configuration.");
            }
            sender.sendMessage("§8======================================================");
            return true;
        }

        if (sub.equals("status")) {
            var status = api.getStatus();
            sender.sendMessage("§8================ §5NexoriaWarps Status §8================");
            sender.sendMessage("§7• Operational State: " + (status.active() ? "§aHealthy & Active" : "§cSuspended"));
            sender.sendMessage("§7• Registered Warps: §d" + status.totalWarps());
            sender.sendMessage("§7• Platform: §f" + plugin.getServer().getName() + " on Java 25");
            sender.sendMessage("§8==========================================================");
            return true;
        }

        if (sub.equals("reload")) {
            if (!sender.hasPermission("nexoria.warps.admin")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.no-permission", "&cYou lack permission: nexoria.warps.admin")));
                return true;
            }
            plugin.reloadConfig();
            api.reload();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.reloaded", "&aWarp configurations successfully reloaded.")));
            return true;
        }

        // Must be a player to teleport
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.only-player", "&cThis command can only be executed by in-game players.")));
            return true;
        }

        WarpsAPI.WarpData warp = api.getWarp(args[0]);
        if (warp == null) {
            String msg = plugin.getConfig().getString("messages.warp-not-found", "&cWarp &d{warp} &cdoes not exist.")
                    .replace("{warp}", args[0]);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }

        Location target = warp.toLocation();
        if (target == null) {
            player.sendMessage("§cThe target world for this warp is not loaded.");
            return true;
        }

        // Check cooldown
        if (!player.hasPermission("nexoria.warps.bypass.cooldown")) {
            if (!api.checkCooldown(player.getUniqueId())) {
                double rem = api.getRemainingCooldown(player.getUniqueId());
                String msg = plugin.getConfig().getString("messages.cooldown", "&cPlease wait &e{seconds}s &cbefore using another warp.")
                        .replace("{seconds}", String.valueOf(rem));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }
        }

        String msg = plugin.getConfig().getString("messages.teleporting", "&7Teleporting to warp &d{warp}&7...")
                .replace("{warp}", warp.name());
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));

        // Teleport player (Folia safe async teleport)
        player.teleportAsync(target);
        return true;
    }

    private List<String> getSuggestions(String[] args) {
        if (args.length <= 1) {
            List<String> list = new ArrayList<>(List.of("list", "help", "status", "reload"));
            if (plugin.getApi() != null) {
                list.addAll(plugin.getApi().getWarpNames());
            }
            return list;
        }
        return List.of();
    }
}
