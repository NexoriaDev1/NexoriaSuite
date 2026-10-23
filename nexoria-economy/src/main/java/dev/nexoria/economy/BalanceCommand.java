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

public final class BalanceCommand extends Command implements BasicCommand {

    private final NexoriaEconomyPlugin plugin;

    public BalanceCommand(NexoriaEconomyPlugin plugin) {
        super("balance", "Check your or another player's balance", "/balance [player]", List.of("bal", "money"));
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
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
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
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
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

        // /balance (self)
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(prefix + "§cSpecify a player from console: /balance <player>");
                return true;
            }
            double bal = api.getBalance(player.getUniqueId());
            String msg = plugin.getConfig().getString("messages.balance", "&7Your balance: &a{balance}")
                    .replace("{balance}", api.format(bal));
            player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }

        // /balance <player>
        String targetName = args[0];
        Player onlineTarget = Bukkit.getPlayer(targetName);
        OfflinePlayer target = onlineTarget != null ? onlineTarget : Bukkit.getOfflinePlayer(targetName);

        if (!api.hasAccount(target.getUniqueId())) {
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.player-not-found", "&cPlayer not found.")));
            return true;
        }

        double bal = api.getBalance(target.getUniqueId());
        String displayName = target.getName() != null ? target.getName() : targetName;

        String msg = plugin.getConfig().getString("messages.balance-other", "&7{player}'s balance: &a{balance}")
                .replace("{player}", displayName)
                .replace("{balance}", api.format(bal));
        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
        return true;
    }
}
