package dev.nexoria.core;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

/**
 * Dual Paper-Brigadier and Bukkit command implementation for NexoriaCore.
 * Compatible with Paper 1.20.6+, Purpur 26.3, and Folia.
 */
public final class CoreCommand extends Command implements BasicCommand {

    private final NexoriaCorePlugin plugin;

    public CoreCommand(NexoriaCorePlugin plugin) {
        super("core", "Primary command entrypoint for NexoriaCore", "/core [status|reload|help]", List.of("nexoriacore"));
        this.plugin = plugin;
        setPermission("nexoria.core.use");
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
        return sender.hasPermission("nexoria.core.use");
    }

    @Override
    public String permission() {
        return "nexoria.core.use";
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
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("nexoria.core.admin")) {
                sender.sendMessage("§cYou lack permission: nexoria.core.admin");
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage("§a[NexoriaCore] Configuration successfully reloaded.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("status")) {
            if (plugin.getApi() == null) {
                sender.sendMessage("§e[NexoriaCore] Status: §7Initializing...");
                return true;
            }
            var status = plugin.getApi().getStatus();
            sender.sendMessage("§b[NexoriaCore] §7Status: " + (status.active() ? "§aOperational" : "§cSuspended"));
            return true;
        }

        sender.sendMessage("§8[§bNexoriaCore§8] §7Running version §f" + plugin.getPluginMeta().getVersion() + " §7on §bJava 25§7.");
        return true;
    }

    private List<String> getSuggestions(String[] args) {
        if (args.length <= 1) {
            return List.of("status", "reload", "help");
        }
        return List.of();
    }
}
