package dev.nexoria.warps;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

/**
 * Command handler for /setwarp.
 */
public final class SetWarpCommand extends Command implements BasicCommand {

    private final NexoriaWarpsPlugin plugin;

    public SetWarpCommand(NexoriaWarpsPlugin plugin) {
        super("setwarp", "Create a new server warp at current location", "/setwarp <name>", List.of());
        this.plugin = plugin;
        setPermission("nexoria.warps.admin");
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        handleExecution(stack.getSender(), args);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        return List.of();
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

    private boolean handleExecution(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexoria.warps.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission", "&cYou lack permission: nexoria.warps.admin")));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly in-game players can set warps.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /setwarp <name>");
            return true;
        }

        String warpName = args[0];
        plugin.getApi().setWarp(warpName, player.getLocation());
        String msg = plugin.getConfig().getString("messages.warp-set", "&aWarp &d{warp} &asuccessfully created at your current position.")
                .replace("{warp}", warpName);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        return true;
    }
}
