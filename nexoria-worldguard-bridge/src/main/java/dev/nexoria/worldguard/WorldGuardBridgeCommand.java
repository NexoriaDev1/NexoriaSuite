package dev.nexoria.worldguard;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public final class WorldGuardBridgeCommand extends Command implements BasicCommand {

    private final NexoriaWorldGuardBridgePlugin plugin;

    public WorldGuardBridgeCommand(NexoriaWorldGuardBridgePlugin plugin) {
        super("wgbridge", "WorldGuard bridge diagnostics", "/wgbridge [test|reload]", List.of("worldguardbridge", "wgb"));
        this.plugin = plugin;
        setPermission("nexoria.worldguard.admin");
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
        if (!sender.hasPermission("nexoria.worldguard.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission", "&cYou lack permission: nexoria.worldguard.admin")));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§8================ §3WorldGuard Bridge §8================");
            sender.sendMessage("§7• §3/wgb test §8- §7Check WorldGuard hook status and current location flags");
            sender.sendMessage("§7• §3/wgb reload §8- §7Re-initialize WorldGuard hook");
            sender.sendMessage("§8=====================================================");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "test" -> {
                boolean active = plugin.getHook().isAvailable();
                sender.sendMessage("§8[§3WorldGuardBridge§8] §7Hook status: " + (active ? "§aConnected" : "§eStandalone Mode (No WG)"));
                if (sender instanceof Player p) {
                    List<String> regions = plugin.getHook().getRegions(p.getLocation());
                    boolean canBuild = plugin.getHook().canBuild(p, p.getLocation());
                    boolean canPvp = plugin.getHook().canPvP(p, p.getLocation());
                    sender.sendMessage("§7• Current regions: §f" + (regions.isEmpty() ? "None (Wilderness)" : String.join(", ", regions)));
                    sender.sendMessage("§7• Can Build: " + (canBuild ? "§aYes" : "§cNo"));
                    sender.sendMessage("§7• Can PvP: " + (canPvp ? "§aYes" : "§cNo"));
                }
            }
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getHook().init();
                sender.sendMessage("§a[WorldGuardBridge] Hook re-initialized.");
            }
            default -> sender.sendMessage("§cUnknown subcommand. Use §f/wgb help");
        }
        return true;
    }

    private List<String> getSuggestions(String[] args) {
        if (args.length <= 1) {
            return List.of("test", "reload");
        }
        return List.of();
    }
}
