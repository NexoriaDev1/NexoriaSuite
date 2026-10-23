package dev.nexoria.economy;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public final class EconomyCommand extends Command implements BasicCommand {

    private final NexoriaEconomyPlugin plugin;

    public EconomyCommand(NexoriaEconomyPlugin plugin) {
        super("economy", "Economy administration command", "/eco <give|take|set|reset|reload>", List.of("eco"));
        this.plugin = plugin;
        setPermission("nexoria.economy.admin");
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        handle(stack.getSender(), args);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        return getSuggestions(args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("nexoria.economy.admin");
    }

    @Override
    public String permission() {
        return "nexoria.economy.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return handle(sender, args);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return getSuggestions(args);
    }

    public boolean handle(CommandSender sender, String[] args) {
        EconomyAPI api = plugin.getApi();
        if (api == null || !api.isActive()) {
            sender.sendMessage("§cEconomy system is not available.");
            return true;
        }

        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&2[&a$&2]&r "));

        if (!sender.hasPermission("nexoria.economy.admin")) {
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to do that.")));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§8================ §aNexoriaEconomy Admin §8================");
            sender.sendMessage("§7• §a/eco give <player> <amount> §8- §7Add funds to player");
            sender.sendMessage("§7• §a/eco take <player> <amount> §8- §7Deduct funds from player");
            sender.sendMessage("§7• §a/eco set <player> <amount>  §8- §7Set player balance");
            sender.sendMessage("§7• §a/eco reset <player>         §8- §7Reset balance to default");
            sender.sendMessage("§7• §a/eco reload                §8- §7Reload config and balances");
            sender.sendMessage("§8=========================================================");
            return true;
        }

        String action = args[0].toLowerCase();

        if (action.equals("reload")) {
            plugin.reloadConfig();
            api.load();
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.reload-success", "&aConfiguration and balances reloaded.")));
            return true;
        }

        if (action.equals("reset")) {
            if (args.length < 2) {
                sender.sendMessage(prefix + "§cUsage: /eco reset <player>");
                return true;
            }
            String targetName = args[1];
            Player onlineTarget = Bukkit.getPlayer(targetName);
            OfflinePlayer target = onlineTarget != null ? onlineTarget : Bukkit.getOfflinePlayer(targetName);

            api.resetBalance(target.getUniqueId());
            String displayName = target.getName() != null ? target.getName() : targetName;
            sender.sendMessage(prefix + "§aReset balance for §e" + displayName + " §ato default (" + api.format(api.getStartingBalance()) + ").");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(prefix + "§cUsage: /eco " + action + " <player> <amount>");
            return true;
        }

        String targetName = args[1];
        Player onlineTarget = Bukkit.getPlayer(targetName);
        OfflinePlayer target = onlineTarget != null ? onlineTarget : Bukkit.getOfflinePlayer(targetName);
        String displayName = target.getName() != null ? target.getName() : targetName;

        double rawAmount;
        try {
            rawAmount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.invalid-amount", "&cInvalid amount specified.")));
            return true;
        }

        double amount = api.round(rawAmount);

        if (amount < 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
            sender.sendMessage(prefix + "§cAmount must be a non-negative number.");
            return true;
        }

        switch (action) {
            case "give" -> {
                api.deposit(target.getUniqueId(), amount);
                double newBal = api.getBalance(target.getUniqueId());
                String msg = plugin.getConfig().getString("messages.admin-give", "&7Added &a{amount} &7to &e{player}&7's account. New balance: &a{balance}")
                        .replace("{amount}", api.format(amount))
                        .replace("{player}", displayName)
                        .replace("{balance}", api.format(newBal));
                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
            }
            case "take" -> {
                boolean ok = api.withdraw(target.getUniqueId(), amount);
                double newBal = api.getBalance(target.getUniqueId());
                if (!ok) {
                    sender.sendMessage(prefix + "§cCannot take " + api.format(amount) + " (Player only has " + api.format(newBal) + ").");
                    return true;
                }
                String msg = plugin.getConfig().getString("messages.admin-take", "&7Took &c{amount} &7from &e{player}&7's account. New balance: &a{balance}")
                        .replace("{amount}", api.format(amount))
                        .replace("{player}", displayName)
                        .replace("{balance}", api.format(newBal));
                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
            }
            case "set" -> {
                api.setBalance(target.getUniqueId(), amount);
                String msg = plugin.getConfig().getString("messages.admin-set", "&7Set &e{player}&7's balance to &a{balance}&7.")
                        .replace("{player}", displayName)
                        .replace("{balance}", api.format(amount));
                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
            }
            default -> sender.sendMessage(prefix + "§cUnknown action: " + action + ". Use: give, take, set, reset, reload");
        }

        return true;
    }

    private List<String> getSuggestions(String[] args) {
        if (args.length <= 1) {
            return List.of("give", "take", "set", "reset", "reload", "help");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("reset"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase("set"))) {
            return List.of("100", "500", "1000", "5000");
        }
        return List.of();
    }
}
