package dev.nexoria.loot;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class LootCommand extends Command implements BasicCommand {

    private final NexoriaLootPlugin plugin;

    public LootCommand(NexoriaLootPlugin plugin) {
        super("loot", "Manage custom drop tables", "/loot [list|reload]", List.of("nexorialoot"));
        this.plugin = plugin;
        setPermission("nexoria.loot.admin");
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
        if (!sender.hasPermission("nexoria.loot.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission", "&cYou lack permission: nexoria.loot.admin")));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§8================ §aNexoriaLoot §8================");
            sender.sendMessage("§7• §a/loot list §8- §7Show configured loot tables");
            sender.sendMessage("§7• §a/loot reload §8- §7Reload loot configuration");
            sender.sendMessage("§8================================================");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "list" -> {
                int mobs = plugin.getLootManager().getAllEntityDrops().size();
                int blocks = plugin.getLootManager().getAllBlockDrops().size();
                sender.sendMessage("§8[§aLoot§8] §7Active loot tables: §e" + mobs + " §7mobs, §e" + blocks + " §7blocks.");
            }
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getLootManager().loadLoot();
                sender.sendMessage("§a[NexoriaLoot] Configuration and loot tables reloaded.");
            }
            default -> sender.sendMessage("§cUnknown subcommand. Use §f/loot help");
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
