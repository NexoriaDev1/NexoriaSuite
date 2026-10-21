package dev.nexoria.customcraft;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class CustomCraftCommand extends Command implements BasicCommand {

    private final NexoriaCustomCraftPlugin plugin;

    public CustomCraftCommand(NexoriaCustomCraftPlugin plugin) {
        super("customcraft", "Manage custom crafting recipes", "/customcraft [list|reload]", List.of("ccraft"));
        this.plugin = plugin;
        setPermission("nexoria.customcraft.admin");
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
        if (!sender.hasPermission("nexoria.customcraft.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission", "&cYou lack permission: nexoria.customcraft.admin")));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§8================ §eNexoriaCustomCraft §8================");
            sender.sendMessage("§7• §e/ccraft list §8- §7Show all active custom recipes");
            sender.sendMessage("§7• §e/ccraft reload §8- §7Reload and re-register recipe definitions");
            sender.sendMessage("§8======================================================");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "list" -> {
                Collection<NamespacedKey> recipes = plugin.getRecipeManager().getRegisteredKeys();
                sender.sendMessage("§8[§eCustomCraft§8] §7Active recipes (§f" + recipes.size() + "§7):");
                for (NamespacedKey k : recipes) {
                    sender.sendMessage("§7- §f" + k.getKey());
                }
            }
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getRecipeManager().loadRecipes();
                sender.sendMessage("§a[NexoriaCustomCraft] Recipes successfully reloaded.");
            }
            default -> sender.sendMessage("§cUnknown subcommand. Use §f/ccraft help");
        }
        return true;
    }

    private List<String> getSuggestions(String[] args) {
        if (args.length <= 1) {
            return List.of("list", "reload");
        }
        return List.of();
    }
}
