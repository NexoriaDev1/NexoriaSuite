package dev.nexoria.economy;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

/**
 * Dual Paper-Brigadier and Bukkit command implementation for NexoriaEconomy.
 * Compatible with Paper 1.20.6+, Purpur 26.3, and Folia.
 */
public final class EconomyCommand extends Command implements BasicCommand {

    private final NexoriaEconomyPlugin plugin;

    public EconomyCommand(NexoriaEconomyPlugin plugin) {
        super("balance", "Primary command entrypoint for NexoriaEconomy", "/balance [status|reload|help]", List.of("economy", "eco"));
        this.plugin = plugin;
        setPermission("nexoria.economy.use");
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
        return sender.hasPermission("nexoria.economy.use");
    }

    @Override
    public String permission() {
        return "nexoria.economy.use";
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
            if (!sender.hasPermission("nexoria.economy.admin")) {
                sender.sendMessage("§cYou lack permission: nexoria.economy.admin");
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage("§a[NexoriaEconomy] Configuration successfully reloaded.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("status")) {
            if (plugin.getApi() == null) {
                sender.sendMessage("§e[NexoriaEconomy] Status: §7Initializing...");
                return true;
            }
            var status = plugin.getApi().getStatus();
            sender.sendMessage("§b[NexoriaEconomy] §7Status: " + (status.active() ? "§aOperational" : "§cSuspended"));
            return true;
        }

        sender.sendMessage("§8[§bNexoriaEconomy§8] §7Running version §f" + plugin.getPluginMeta().getVersion() + " §7on §bJava 25§7.");
        return true;
    }

    private List<String> getSuggestions(String[] args) {
        if (args.length <= 1) {
            return List.of("status", "reload", "help");
        }
        return List.of();
    }
}
