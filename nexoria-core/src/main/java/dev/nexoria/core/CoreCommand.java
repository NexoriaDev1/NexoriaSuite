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
        super("core", "Primary command entrypoint for NexoriaCore", "/core [status|reload|info|help]", List.of("nexoriacore"));
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
            boolean folia = plugin.getApi().isFolia();
            sender.sendMessage("§8================ §bNexoriaCore Status §8================");
            sender.sendMessage("§7• Operational State: " + (status.active() ? "§aHealthy & Active" : "§cSuspended"));
            sender.sendMessage("§7• Server Engine: §f" + plugin.getServer().getName() + " " + plugin.getServer().getVersion());
            sender.sendMessage("§7• Folia Multithreading: " + (folia ? "§aEnabled (Regional)" : "§eStandard Paper/Purpur"));
            sender.sendMessage("§7• Registered Active Sessions: §b" + status.activeSessions());
            sender.sendMessage("§7• Java Specification: §f" + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
            sender.sendMessage("§8=====================================================");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("info")) {
            long maxMem = Runtime.getRuntime().maxMemory() / (1024 * 1024);
            long totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024);
            long freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024);
            long usedMem = totalMem - freeMem;

            sender.sendMessage("§8================ §bNexoriaCore System Info §8================");
            sender.sendMessage("§7• Plugin Version: §f" + plugin.getPluginMeta().getVersion());
            sender.sendMessage("§7• Allocated Memory: §b" + usedMem + "MB §7/ §f" + maxMem + "MB");
            sender.sendMessage("§7• Available Processors: §b" + Runtime.getRuntime().availableProcessors());
            sender.sendMessage("§7• Operating System: §f" + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
            sender.sendMessage("§8============================================================");
            return true;
        }

        // Help menu (default when running /core or /core help)
        sender.sendMessage("§8================ §bNexoriaCore Help §8================");
        sender.sendMessage("§7• §b/core status §8- §7Display active thread health and engine state.");
        sender.sendMessage("§7• §b/core info   §8- §7Display system memory, OS, and JVM metrics.");
        sender.sendMessage("§7• §b/core reload §8- §7Reload configuration files and data caches.");
        sender.sendMessage("§7• §b/core help   §8- §7Show this command reference list.");
        sender.sendMessage("§8====================================================");
        return true;
    }

    private List<String> getSuggestions(String[] args) {
        if (args.length <= 1) {
            return List.of("status", "info", "reload", "help");
        }
        return List.of();
    }
}
