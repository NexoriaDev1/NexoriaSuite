package dev.nexoria.economy;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public final class PayCommand extends Command implements BasicCommand {

    private final NexoriaEconomyPlugin plugin;

    public PayCommand(NexoriaEconomyPlugin plugin) {
        super("pay", "Send money to another player", "/pay <player> <amount>", List.of());
        this.plugin = plugin;
        setPermission("nexoria.economy.pay");
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        handle(stack.getSender(), args);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 2) {
            return List.of("10", "50", "100", "500", "1000");
        }
        return List.of();
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("nexoria.economy.pay");
    }

    @Override
    public String permission() {
        return "nexoria.economy.pay";
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return handle(sender, args);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 2) {
            return List.of("10", "50", "100", "500", "1000");
        }
        return List.of();
    }

    private boolean handle(CommandSender sender, String[] args) {
        EconomyAPI api = plugin.getApi();
        if (api == null || !api.isActive()) {
            sender.sendMessage("§cEconomy system is not available.");
            return true;
        }

        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&2[&a$&2]&r "));

        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.only-players", "&cThis command can only be used by players.")));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(prefix + "§cUsage: /pay <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.player-not-found", "&cPlayer not found.")));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.cannot-pay-self", "&cYou cannot send money to yourself.")));
            return true;
        }

        double rawAmount;
        try {
            rawAmount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.invalid-amount", "&cInvalid amount specified.")));
            return true;
        }

        double amount = api.round(rawAmount);
        double minPay = api.getMinPayAmount();

        if (amount <= 0 || Double.isNaN(amount) || Double.isInfinite(amount) || amount < minPay) {
            player.sendMessage(prefix + "§cAmount must be at least " + api.format(minPay) + ".");
            return true;
        }

        if (!api.has(player.getUniqueId(), amount)) {
            String msg = plugin.getConfig().getString("messages.insufficient-funds", "&cYou don't have enough money. (Required: {needed})")
                    .replace("{needed}", api.format(amount));
            player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }

        boolean withdrawn = api.withdraw(player.getUniqueId(), amount);
        if (!withdrawn) {
            player.sendMessage(prefix + "§cTransaction failed. Please try again.");
            return true;
        }

        api.deposit(target.getUniqueId(), amount);

        String sentMsg = plugin.getConfig().getString("messages.pay-sent", "&7You sent &a{amount} &7to &e{player}&7.")
                .replace("{amount}", api.format(amount))
                .replace("{player}", target.getName());
        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', sentMsg));

        String recvMsg = plugin.getConfig().getString("messages.pay-received", "&7You received &a{amount} &7from &e{player}&7.")
                .replace("{amount}", api.format(amount))
                .replace("{player}", player.getName());
        target.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', recvMsg));

        return true;
    }
}
