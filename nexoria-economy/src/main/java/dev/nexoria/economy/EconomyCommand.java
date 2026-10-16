package dev.nexoria.economy;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

/**
 * Dual Paper-Brigadier and Bukkit command implementation for NexoriaEconomy.
 * Handles /balance, /economy, /eco commands.
 */
public final class EconomyCommand extends Command implements BasicCommand {

    private final NexoriaEconomyPlugin plugin;

    public EconomyCommand(NexoriaEconomyPlugin plugin) {
        super("balance", "Primary command entrypoint for NexoriaEconomy", "/balance [player|help|status|reload|give|take|set]", List.of("economy", "eco"));
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
        EconomyAPI api = plugin.getApi();
        if (api == null) {
            sender.sendMessage("§e[NexoriaEconomy] Economy service is still initializing...");
            return true;
        }

        // /balance (without arguments)
        if (args.length == 0) {
            if (sender instanceof Player player) {
                double bal = api.getBalance(player.getUniqueId());
                sender.sendMessage("§8[§aNexoriaEconomy§8] §7Your balance: §a" + api.format(bal));
            } else {
                sender.sendMessage("§8[§aNexoriaEconomy§8] §7Console has no personal account. Use: §f/balance <player>");
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        // /eco help | /balance help
        if (sub.equals("help")) {
            sender.sendMessage("§8================ §aNexoriaEconomy Help §8================");
            sender.sendMessage("§7• §a/balance §8- §7Check your current balance.");
            sender.sendMessage("§7• §a/balance <player> §8- §7Check another player's balance.");
            sender.sendMessage("§7• §a/eco give <player> <amount> §8- §7Deposit currency into player account.");
            sender.sendMessage("§7• §a/eco take <player> <amount> §8- §7Deduct currency from player account.");
            sender.sendMessage("§7• §a/eco set <player> <amount>  §8- §7Set exact balance for player account.");
            sender.sendMessage("§7• §a/eco status §8- §7Display economy health and total circulation.");
            sender.sendMessage("§7• §a/eco reload §8- §7Reload configuration and currency files.");
            sender.sendMessage("§8======================================================");
            return true;
        }

        // /eco status
        if (sub.equals("status")) {
            var status = api.getStatus();
            sender.sendMessage("§8================ §aNexoriaEconomy Status §8================");
            sender.sendMessage("§7• Operational State: " + (status.active() ? "§aHealthy & Active" : "§cSuspended"));
            sender.sendMessage("§7• Registered Accounts: §f" + status.totalAccounts());
            sender.sendMessage("§7• Total Circulation: §a" + api.format(status.totalCirculation()));
            sender.sendMessage("§7• Platform: §f" + plugin.getServer().getName() + " on Java 25");
            sender.sendMessage("§8=========================================================");
            return true;
        }

        // /eco reload
        if (sub.equals("reload")) {
            if (!sender.hasPermission("nexoria.economy.admin")) {
                sender.sendMessage("§cYou lack permission: nexoria.economy.admin");
                return true;
            }
            plugin.reloadConfig();
            api.loadData();
            sender.sendMessage("§a[NexoriaEconomy] Configuration successfully reloaded.");
            return true;
        }

        // Admin commands: give, take, set
        if (sub.equals("give") || sub.equals("take") || sub.equals("set")) {
            if (!sender.hasPermission("nexoria.economy.admin")) {
                sender.sendMessage("§cYou lack permission: nexoria.economy.admin");
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage("§cUsage: /eco " + sub + " <player> <amount>");
                return true;
            }

            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            double amount;
            try {
                amount = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid amount: " + args[2]);
                return true;
            }

            if (amount < 0) {
                sender.sendMessage("§cAmount must be a positive number.");
                return true;
            }

            String targetName = target.getName() != null ? target.getName() : args[1];

            switch (sub) {
                case "give" -> {
                    api.deposit(target.getUniqueId(), amount);
                    sender.sendMessage("§a[NexoriaEconomy] Added " + api.format(amount) + " to §f" + targetName + "§a. New balance: §f" + api.format(api.getBalance(target.getUniqueId())));
                }
                case "take" -> {
                    boolean ok = api.withdraw(target.getUniqueId(), amount);
                    if (ok) {
                        sender.sendMessage("§a[NexoriaEconomy] Deducted " + api.format(amount) + " from §f" + targetName + "§a. New balance: §f" + api.format(api.getBalance(target.getUniqueId())));
                    } else {
                        sender.sendMessage("§c[NexoriaEconomy] Cannot withdraw " + api.format(amount) + " from " + targetName + " (insufficient funds, current: " + api.format(api.getBalance(target.getUniqueId())) + ").");
                    }
                }
                case "set" -> {
                    api.setBalance(target.getUniqueId(), amount);
                    sender.sendMessage("§a[NexoriaEconomy] Set balance of §f" + targetName + " §ato §f" + api.format(amount));
                }
            }
            return true;
        }

        // Check another player's balance: /balance <player>
        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        String targetName = target.getName() != null ? target.getName() : args[0];
        double bal = api.getBalance(target.getUniqueId());
        sender.sendMessage("§8[§aNexoriaEconomy§8] §7Balance of §f" + targetName + "§7: §a" + api.format(bal));
        return true;
    }

    private List<String> getSuggestions(String[] args) {
        if (args.length <= 1) {
            return List.of("help", "status", "reload", "give", "take", "set");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase("set"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
