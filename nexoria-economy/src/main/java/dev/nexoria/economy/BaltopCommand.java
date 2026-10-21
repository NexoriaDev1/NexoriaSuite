package dev.nexoria.economy;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class BaltopCommand extends Command implements BasicCommand {

    private final NexoriaEconomyPlugin plugin;

    public BaltopCommand(NexoriaEconomyPlugin plugin) {
        super("balancetop", "View the wealthiest players on the server", "/baltop", List.of("baltop"));
        this.plugin = plugin;
        setPermission("nexoria.economy.use");
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        handle(stack.getSender());
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        return List.of();
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("nexoria.economy.use");
    }

    @Override
    public String permission() {
        return "nexoria.economy.use";
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return handle(sender);
    }

    private boolean handle(CommandSender sender) {
        EconomyAPI api = plugin.getApi();
        if (api == null || !api.isActive()) {
            sender.sendMessage("§cEconomy system is not available.");
            return true;
        }

        int limit = plugin.getConfig().getInt("baltop.limit", 10);
        var top = api.getTopBalances(limit);

        sender.sendMessage("§8================ §aTop Balances §8================");
        if (top.isEmpty()) {
            sender.sendMessage("§7No registered accounts found.");
        } else {
            int rank = 1;
            for (var entry : top) {
                String color = switch (rank) {
                    case 1 -> "§6";
                    case 2 -> "§f";
                    case 3 -> "§c";
                    default -> "§7";
                };
                sender.sendMessage(String.format("§8#%d %s%s §8- §a%s", rank, color, entry.name(), api.format(entry.balance())));
                rank++;
            }
        }
        sender.sendMessage(String.format("§8Total circulation: §a%s §8| §7Accounts: §f%d", api.format(api.getTotalCirculation()), api.getTotalAccounts()));
        sender.sendMessage("§8================================================");
        return true;
    }
}
