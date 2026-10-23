package dev.nexoria.economy;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class BaltopCommand extends Command implements BasicCommand {

    private final NexoriaEconomyPlugin plugin;

    public BaltopCommand(NexoriaEconomyPlugin plugin) {
        super("balancetop", "View the wealthiest players on the server", "/baltop [page]", List.of("baltop"));
        this.plugin = plugin;
        setPermission("nexoria.economy.use");
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        handle(stack.getSender(), args);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        if (args.length <= 1) {
            return List.of("1", "2", "3");
        }
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
        return handle(sender, args);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length <= 1) {
            return List.of("1", "2", "3");
        }
        return List.of();
    }

    private boolean handle(CommandSender sender, String[] args) {
        EconomyAPI api = plugin.getApi();
        if (api == null || !api.isActive()) {
            sender.sendMessage("§cEconomy system is not available.");
            return true;
        }

        int perPage = Math.max(1, plugin.getConfig().getInt("baltop.limit", 10));
        int maxPages = Math.max(1, api.getMaxBaltopPages(perPage));
        int page = 1;

        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        if (page < 1) page = 1;
        if (page > maxPages) page = maxPages;

        var top = api.getTopBalances(page, perPage);

        sender.sendMessage(String.format("§8================ §aTop Balances §7(%d/%d) §8================", page, maxPages));
        if (top.isEmpty()) {
            sender.sendMessage("§7No registered accounts found.");
        } else {
            int rank = (page - 1) * perPage + 1;
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
