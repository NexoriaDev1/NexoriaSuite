package dev.nexoria.punish;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Moderation command dispatcher handling /ban, /unban, /mute, /unmute, /kick, /punish.
 */
public final class PunishCommand extends Command implements BasicCommand {

    private final NexoriaPunishPlugin plugin;

    public PunishCommand(NexoriaPunishPlugin plugin) {
        super("punish", "Primary command entrypoint for NexoriaPunish", "/punish [status|reload|help]", List.of("nexoriapunish"));
        this.plugin = plugin;
        setPermission("nexoria.punish.admin");
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        handleExecution(stack.getSender(), "punish", args);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        return getSuggestions(args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("nexoria.punish.admin");
    }

    @Override
    public String permission() {
        return "nexoria.punish.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return handleExecution(sender, commandLabel, args);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return getSuggestions(args);
    }

    public boolean handleExecution(CommandSender sender, String label, String[] args) {
        PunishAPI api = plugin.getApi();
        if (api == null) {
            sender.sendMessage("§e[NexoriaPunish] Moderation service is still initializing...");
            return true;
        }

        if (!sender.hasPermission("nexoria.punish.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission", "&cYou lack permission: nexoria.punish.admin")));
            return true;
        }

        String staff = sender.getName();
        String cmd = label.toLowerCase();

        // Direct /ban command
        if (cmd.equals("ban")) {
            if (args.length < 1) {
                sender.sendMessage("§cUsage: /ban <player> [reason]");
                return true;
            }
            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Banned by administrator";
            api.ban(target.getUniqueId(), target.getName() != null ? target.getName() : args[0], staff, reason);

            if (target.isOnline() && target.getPlayer() != null) {
                String kickMsg = plugin.getConfig().getString("messages.banned-kick", "&cYou are permanently banned from this server.\n&7Reason: &f{reason}\n&7Staff: &e{staff}")
                        .replace("{reason}", reason)
                        .replace("{staff}", staff);
                target.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', kickMsg));
            }

            String bcast = plugin.getConfig().getString("messages.player-banned", "&e{player} &7was permanently banned by &c{staff}&7. Reason: &f{reason}")
                    .replace("{player}", target.getName() != null ? target.getName() : args[0])
                    .replace("{staff}", staff)
                    .replace("{reason}", reason);
            plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', bcast));
            return true;
        }

        // Direct /unban command
        if (cmd.equals("unban")) {
            if (args.length < 1) {
                sender.sendMessage("§cUsage: /unban <player>");
                return true;
            }
            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            boolean ok = api.unban(target.getUniqueId());
            if (ok) {
                String bcast = plugin.getConfig().getString("messages.player-unbanned", "&e{player} &7was unbanned by &a{staff}&7.")
                        .replace("{player}", target.getName() != null ? target.getName() : args[0])
                        .replace("{staff}", staff);
                plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', bcast));
            } else {
                sender.sendMessage("§cPlayer is not currently banned.");
            }
            return true;
        }

        // Direct /mute command
        if (cmd.equals("mute")) {
            if (args.length < 1) {
                sender.sendMessage("§cUsage: /mute <player> [reason]");
                return true;
            }
            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Muted by administrator";
            api.mute(target.getUniqueId(), target.getName() != null ? target.getName() : args[0], staff, reason);

            String bcast = plugin.getConfig().getString("messages.player-muted", "&e{player} &7was muted by &c{staff}&7. Reason: &f{reason}")
                    .replace("{player}", target.getName() != null ? target.getName() : args[0])
                    .replace("{staff}", staff)
                    .replace("{reason}", reason);
            plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', bcast));
            return true;
        }

        // Direct /unmute command
        if (cmd.equals("unmute")) {
            if (args.length < 1) {
                sender.sendMessage("§cUsage: /unmute <player>");
                return true;
            }
            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            boolean ok = api.unmute(target.getUniqueId());
            if (ok) {
                String bcast = plugin.getConfig().getString("messages.player-unmuted", "&e{player} &7was unmuted by &a{staff}&7.")
                        .replace("{player}", target.getName() != null ? target.getName() : args[0])
                        .replace("{staff}", staff);
                plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', bcast));
            } else {
                sender.sendMessage("§cPlayer is not currently muted.");
            }
            return true;
        }

        // Direct /kick command
        if (cmd.equals("kick")) {
            if (args.length < 1) {
                sender.sendMessage("§cUsage: /kick <player> [reason]");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer not online.");
                return true;
            }
            String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Kicked by staff";
            target.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&cKicked: &f" + reason));

            String bcast = plugin.getConfig().getString("messages.player-kicked", "&e{player} &7was kicked by &c{staff}&7. Reason: &f{reason}")
                    .replace("{player}", target.getName())
                    .replace("{staff}", staff)
                    .replace("{reason}", reason);
            plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', bcast));
            return true;
        }

        // /punish menu and status
        if (args.length > 0) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "status" -> {
                    var status = api.getStatus();
                    sender.sendMessage("§8================ §cNexoriaPunish Status §8================");
                    sender.sendMessage("§7• Operational State: " + (status.active() ? "§aHealthy & Active" : "§cSuspended"));
                    sender.sendMessage("§7• Total Active Bans: §c" + status.totalBans());
                    sender.sendMessage("§7• Total Active Mutes: §e" + status.totalMutes());
                    sender.sendMessage("§7• Platform: §f" + plugin.getServer().getName() + " on Java 25");
                    sender.sendMessage("§8==========================================================");
                    return true;
                }
                case "reload" -> {
                    plugin.reloadConfig();
                    api.loadData();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.reloaded", "&a[NexoriaPunish] Punishment database and configuration reloaded.")));
                    return true;
                }
            }
        }

        // /punish help
        sender.sendMessage("§8================ §cNexoriaPunish Commands §8================");
        sender.sendMessage("§7• §c/ban <player> [reason]   §8- §7Permanently ban a player.");
        sender.sendMessage("§7• §c/unban <player>          §8- §7Unban a previously banned player.");
        sender.sendMessage("§7• §c/mute <player> [reason]  §8- §7Silence a player from global chat.");
        sender.sendMessage("§7• §c/unmute <player>        §8- §7Restore chat ability for player.");
        sender.sendMessage("§7• §c/kick <player> [reason]  §8- §7Disconnect an online player.");
        sender.sendMessage("§7• §c/punish status           §8- §7View punishment logs and metrics.");
        sender.sendMessage("§7• §c/punish reload           §8- §7Reload configurations from disk.");
        sender.sendMessage("§8============================================================");
        return true;
    }

    private List<String> getSuggestions(String[] args) {
        if (args.length <= 1) {
            return List.of("status", "reload", "help");
        }
        return List.of();
    }
}
