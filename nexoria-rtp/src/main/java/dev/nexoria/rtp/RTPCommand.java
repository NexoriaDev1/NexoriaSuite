package dev.nexoria.rtp;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

/**
 * Universal RTP command implementing Brigadier and Bukkit fallback.
 */
public final class RTPCommand extends Command implements BasicCommand {

    private final NexoriaRTPPlugin plugin;

    public RTPCommand(NexoriaRTPPlugin plugin) {
        super("rtp", "Teleport randomly into the wilderness", "/rtp [help|status|reload]", List.of("wild", "wilderness"));
        this.plugin = plugin;
        setPermission("nexoria.rtp.use");
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
        return sender.hasPermission("nexoria.rtp.use");
    }

    @Override
    public String permission() {
        return "nexoria.rtp.use";
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
        RTPAPI api = plugin.getApi();
        if (api == null) {
            sender.sendMessage("§e[NexoriaRTP] RTP service is still initializing...");
            return true;
        }

        if (args.length > 0) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "help" -> {
                    sender.sendMessage("§8================ §aNexoriaRTP Help §8================");
                    sender.sendMessage("§7• §a/rtp §8- §7Teleport to an unexplored random wilderness spot.");
                    sender.sendMessage("§7• §a/rtp status §8- §7Show total teleports and configuration info.");
                    if (sender.hasPermission("nexoria.rtp.admin")) {
                        sender.sendMessage("§7• §a/rtp reload §8- §7Reload RTP settings and bounds.");
                    }
                    sender.sendMessage("§8=====================================================");
                    return true;
                }
                case "status" -> {
                    var status = api.getStatus();
                    sender.sendMessage("§8================ §aNexoriaRTP Status §8================");
                    sender.sendMessage("§7• Operational State: " + (status.active() ? "§aHealthy & Active" : "§cSuspended"));
                    sender.sendMessage("§7• Wilderness Jumps Executed: §f" + status.totalTeleports());
                    sender.sendMessage("§7• Platform: §f" + plugin.getServer().getName() + " on Java 25");
                    sender.sendMessage("§8========================================================");
                    return true;
                }
                case "reload" -> {
                    if (!sender.hasPermission("nexoria.rtp.admin")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.getConfig().getString("messages.no-permission", "&cYou lack permission: nexoria.rtp.admin")));
                        return true;
                    }
                    plugin.reloadConfig();
                    api.reload();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.reloaded", "&a[NexoriaRTP] Configuration successfully reloaded.")));
                    return true;
                }
            }
        }

        // Must be a player to teleport
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.only-player", "&cThis command can only be executed by in-game players.")));
            return true;
        }

        // Check cooldown
        if (!player.hasPermission("nexoria.rtp.bypass.cooldown")) {
            if (!api.checkCooldown(player.getUniqueId())) {
                double rem = api.getRemainingCooldown(player.getUniqueId());
                String msg = plugin.getConfig().getString("messages.cooldown", "&cPlease wait &e{seconds}s &cbefore using random teleport again.")
                        .replace("{seconds}", String.valueOf(rem));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.searching", "&7Searching for a safe wilderness location...")));

        api.findSafeLocation(player.getWorld()).thenAccept(loc -> {
            if (loc == null) {
                int attempts = plugin.getConfig().getInt("rtp.max-attempts", 15);
                String msg = plugin.getConfig().getString("messages.failed", "&cCould not find a safe wilderness spot in {attempts} attempts. Please try again.")
                        .replace("{attempts}", String.valueOf(attempts));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                return;
            }

            // Folia safe asynchronous teleport
            player.teleportAsync(loc).thenRun(() -> {
                String msg = plugin.getConfig().getString("messages.teleported", "&aTeleported to &fX: {x}, Y: {y}, Z: {z}&a!")
                        .replace("{x}", String.valueOf(loc.getBlockX()))
                        .replace("{y}", String.valueOf(loc.getBlockY()))
                        .replace("{z}", String.valueOf(loc.getBlockZ()));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            });
        });

        return true;
    }

    private List<String> getSuggestions(String[] args) {
        if (args.length <= 1) {
            return List.of("help", "status", "reload");
        }
        return List.of();
    }
}
