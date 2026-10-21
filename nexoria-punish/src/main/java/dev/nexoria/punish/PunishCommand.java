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

public final class PunishCommand extends Command implements BasicCommand {

    private final NexoriaPunishPlugin plugin;
    private final String action;

    public PunishCommand(NexoriaPunishPlugin plugin, String name, String desc, String usage, List<String> aliases, String action) {
        super(name, desc, usage, aliases);
        this.plugin = plugin;
        this.action = action;
        setPermission("nexoria.punish.admin");
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        handleExecution(stack.getSender(), this.action, args);
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
        return handleExecution(sender, this.action, args);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return getSuggestions(args);
    }

    public boolean handleExecution(CommandSender sender, String label, String[] args) {
        PunishAPI api = plugin.getApi();
        if (api == null) {
            sender.sendMessage(ChatColor.RED + "Punishment service is initializing...");
            return true;
        }

        if (!sender.hasPermission("nexoria.punish.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission", "&cYou lack permission: nexoria.punish.admin")));
            return true;
        }

        String staff = sender.getName();
        String cmd = label.toLowerCase();

        // Direct /ban
        if (cmd.equals("ban")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /ban <player> [reason]");
                return true;
            }
            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            String targetName = target.getName() != null ? target.getName() : args[0];
            String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Banned by administrator";
            api.ban(target.getUniqueId(), targetName, staff, reason);

            if (target.isOnline() && target.getPlayer() != null) {
                String kickMsg = plugin.getConfig().getString("messages.banned-kick", "&cYou are permanently banned.\n&7Reason: &f{reason}\n&7Staff: &e{staff}")
                        .replace("{reason}", reason)
                        .replace("{staff}", staff);
                target.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', kickMsg));
            }

            String bcast = plugin.getConfig().getString("messages.player-banned", "&e{player} &7was permanently banned by &c{staff}&7. Reason: &f{reason}")
                    .replace("{player}", targetName)
                    .replace("{staff}", staff)
                    .replace("{reason}", reason);
            plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', bcast));
            return true;
        }

        // Direct /unban
        if (cmd.equals("unban")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /unban <player>");
                return true;
            }
            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            String targetName = target.getName() != null ? target.getName() : args[0];
            boolean ok = api.unban(target.getUniqueId());
            if (ok) {
                String bcast = plugin.getConfig().getString("messages.player-unbanned", "&e{player} &7was unbanned by &a{staff}&7.")
                        .replace("{player}", targetName)
                        .replace("{staff}", staff);
                plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', bcast));
            } else {
                sender.sendMessage(ChatColor.RED + "Player is not currently banned.");
            }
            return true;
        }

        // Direct /mute
        if (cmd.equals("mute")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /mute <player> [reason]");
                return true;
            }
            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            String targetName = target.getName() != null ? target.getName() : args[0];
            String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Muted by staff";
            api.mute(target.getUniqueId(), targetName, staff, reason);

            String bcast = plugin.getConfig().getString("messages.player-muted", "&e{player} &7was muted by &c{staff}&7. Reason: &f{reason}")
                    .replace("{player}", targetName)
                    .replace("{staff}", staff)
                    .replace("{reason}", reason);
            plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', bcast));
            return true;
        }

        // Direct /unmute
        if (cmd.equals("unmute")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /unmute <player>");
                return true;
            }
            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            String targetName = target.getName() != null ? target.getName() : args[0];
            boolean ok = api.unmute(target.getUniqueId());
            if (ok) {
                String bcast = plugin.getConfig().getString("messages.player-unmuted", "&e{player} &7was unmuted by &a{staff}&7.")
                        .replace("{player}", targetName)
                        .replace("{staff}", staff);
                plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', bcast));
            } else {
                sender.sendMessage(ChatColor.RED + "Player is not currently muted.");
            }
            return true;
        }

        // Direct /kick
        if (cmd.equals("kick")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /kick <player> [reason]");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(ChatColor.RED + "Player is not online.");
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
                    sender.sendMessage(ChatColor.DARK_GRAY + "================ " + ChatColor.RED + "NexoriaPunish Status " + ChatColor.DARK_GRAY + "================");
                    sender.sendMessage(ChatColor.GRAY + "- Operational: " + (status.active() ? ChatColor.GREEN + "Active" : ChatColor.RED + "Suspended"));
                    sender.sendMessage(ChatColor.GRAY + "- Total Active Bans: " + ChatColor.RED + status.totalBans());
                    sender.sendMessage(ChatColor.GRAY + "- Total Active Mutes: " + ChatColor.YELLOW + status.totalMutes());
                    sender.sendMessage(ChatColor.DARK_GRAY + "==========================================================");
                    return true;
                }
                case "reload" -> {
                    plugin.reloadConfig();
                    api.loadData();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.reloaded", "&aPunishment database and configuration reloaded.")));
                    return true;
                }
            }
        }

        // /punish help
        sender.sendMessage(ChatColor.DARK_GRAY + "================ " + ChatColor.RED + "NexoriaPunish Commands " + ChatColor.DARK_GRAY + "================");
        sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + "/ban <player> [reason]   " + ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + "Permanently ban a player.");
        sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + "/unban <player>          " + ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + "Unban a previously banned player.");
        sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + "/mute <player> [reason]  " + ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + "Silence a player from global chat.");
        sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + "/unmute <player>        " + ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + "Restore chat ability for player.");
        sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + "/kick <player> [reason]  " + ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + "Disconnect an online player.");
        sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + "/punish status           " + ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + "View active punishment statistics.");
        sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + "/punish reload           " + ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + "Reload configurations from disk.");
        sender.sendMessage(ChatColor.DARK_GRAY + "============================================================");
        return true;
    }

    private List<String> getSuggestions(String[] args) {
        if (args.length <= 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
