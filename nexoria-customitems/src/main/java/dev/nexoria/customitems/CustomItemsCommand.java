package dev.nexoria.customitems;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CustomItemsCommand extends Command implements BasicCommand {

    private final NexoriaCustomItemsPlugin plugin;

    public CustomItemsCommand(NexoriaCustomItemsPlugin plugin) {
        super("customitems", "Manage custom items", "/customitems [give|list|reload]", List.of("citems", "ci"));
        this.plugin = plugin;
        setPermission("nexoria.customitems.admin");
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
        if (!sender.hasPermission("nexoria.customitems.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission", "&cYou lack permission: nexoria.customitems.admin")));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§8================ §6NexoriaCustomItems §8================");
            sender.sendMessage("§7• §6/ci list §8- §7List all configured custom items");
            sender.sendMessage("§7• §6/ci give <player> <item_id> [amount] §8- §7Give custom item");
            sender.sendMessage("§7• §6/ci reload §8- §7Reload items configuration");
            sender.sendMessage("§8=====================================================");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "list" -> {
                Collection<CustomItem> all = plugin.getItemManager().getAllItems().values();
                sender.sendMessage("§8[§6Items§8] §7Configured items (§f" + all.size() + "§7):");
                for (CustomItem ci : all) {
                    sender.sendMessage("§7- §e" + ci.getId() + " §8(§f" + ci.getMaterial().name() + "§8) §7Ability: §b" + ci.getAbility());
                }
            }
            case "give" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /ci give <player> <item_id> [amount]");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found: " + args[1]);
                    return true;
                }
                String itemId = args[2].toLowerCase();
                CustomItem ci = plugin.getItemManager().getItem(itemId);
                if (ci == null) {
                    sender.sendMessage("§cUnknown custom item: " + itemId);
                    return true;
                }
                int amount = 1;
                if (args.length >= 4) {
                    try {
                        amount = Math.max(1, Integer.parseInt(args[3]));
                    } catch (NumberFormatException ignored) {}
                }
                ItemStack stack = ci.createItemStack(plugin, amount);
                target.getInventory().addItem(stack);
                sender.sendMessage("§aGave §e" + amount + "x " + ci.getId() + " §ato §f" + target.getName() + "§a.");
                target.sendMessage("§aYou received §e" + amount + "x " + ChatColor.translateAlternateColorCodes('&', ci.getName()) + "§a!");
            }
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getItemManager().loadItems();
                sender.sendMessage("§a[NexoriaCustomItems] Configuration and items reloaded.");
            }
            default -> sender.sendMessage("§cUnknown subcommand. Use §f/ci help");
        }
        return true;
    }

    private List<String> getSuggestions(String[] args) {
        if (args.length <= 1) {
            return List.of("give", "list", "reload");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                names.add(p.getName());
            }
            return names;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return new ArrayList<>(plugin.getItemManager().getAllItems().keySet());
        }
        return List.of();
    }
}
