package dev.nexoria.warps;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Command handler for /delwarp.
 */
public final class DelWarpCommand extends Command implements BasicCommand {

    private final NexoriaWarpsPlugin plugin;

    public DelWarpCommand(NexoriaWarpsPlugin plugin) {
        super("delwarp", "Delete an existing server warp", "/delwarp <name>", List.of());
        this.plugin = plugin;
        setPermission("nexoria.warps.admin");
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
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("nexoria.warps.admin");
    }

    @Override
    public String permission() {
        return "nexoria.warps.admin";
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
        if (!sender.hasPermission("nexoria.warps.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission", "&cYou lack permission: nexoria.warps.admin")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /delwarp <name>");
            return true;
        }

        String warpName = args[0];
        boolean deleted = plugin.getApi().deleteWarp(warpName);
        if (deleted) {
            String msg = plugin.getConfig().getString("messages.warp-deleted", "&cWarp &d{warp} &ahas been deleted.")
                    .replace("{warp}", warpName);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        } else {
            String msg = plugin.getConfig().getString("messages.warp-not-found", "&cWarp &d{warp} &cdoes not exist.")
                    .replace("{warp}", warpName);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
        return true;
    }

    private List<String> getSuggestions(String[] args) {
        if (args.length <= 1 && plugin.getApi() != null) {
            return new ArrayList<>(plugin.getApi().getWarpNames());
        }
        return List.of();
    }
}
