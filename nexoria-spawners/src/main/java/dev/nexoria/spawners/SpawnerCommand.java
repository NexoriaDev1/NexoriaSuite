package dev.nexoria.spawners;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpawnerCommand implements BasicCommand, CommandExecutor, TabCompleter {

    private final NexoriaSpawnersPlugin plugin;

    public SpawnerCommand(NexoriaSpawnersPlugin plugin) {
        this.plugin = plugin;
    }

    private String msg(String key) {
        String p = plugin.getConfig().getString("messages.prefix", "&8[&dSpawners&8] ");
        String m = plugin.getConfig().getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', p + m);
    }

    private void handleCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexoria.spawners.admin")) {
            sender.sendMessage(msg("no-permission"));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(msg("reloaded"));
            return;
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("give")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                return;
            }

            EntityType type;
            try {
                type = EntityType.valueOf(args[2].toUpperCase());
            } catch (Exception e) {
                sender.sendMessage(msg("invalid-entity").replace("{type}", args[2]));
                return;
            }

            int amount = 1;
            if (args.length >= 4) {
                try {
                    amount = Math.max(1, Integer.parseInt(args[3]));
                } catch (Exception ignored) {}
            }

            ItemStack spawner = plugin.getSpawnerManager().createSpawnerItem(type, amount);
            target.getInventory().addItem(spawner);

            String mobName = plugin.getSpawnerManager().formatName(type.name());
            sender.sendMessage(msg("spawner-given").replace("{amount}", String.valueOf(amount)).replace("{type}", mobName).replace("{player}", target.getName()));
            target.sendMessage(msg("spawner-received").replace("{amount}", String.valueOf(amount)).replace("{type}", mobName));
            return;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("set")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use /spawner set.");
                return;
            }

            Block targetBlock = player.getTargetBlockExact(5);
            if (targetBlock == null || targetBlock.getType() != Material.SPAWNER) {
                player.sendMessage(ChatColor.RED + "You must look directly at a spawner block.");
                return;
            }

            EntityType type;
            try {
                type = EntityType.valueOf(args[1].toUpperCase());
            } catch (Exception e) {
                sender.sendMessage(msg("invalid-entity").replace("{type}", args[1]));
                return;
            }

            plugin.getSpawnerManager().setBlockSpawnerType(targetBlock, type);
            String mobName = plugin.getSpawnerManager().formatName(type.name());
            player.sendMessage(msg("changed").replace("{type}", mobName));
            return;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&dNexoriaSpawners &7v" + plugin.getPluginMeta().getVersion()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/spawner give <player> <entity> [amount] &7- Give spawner"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/spawner set <entity>                   &7- Change looked spawner"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/spawner reload                         &7- Reload config"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        handleCommand(source.getSender(), args);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return getCompletions(source.getSender(), args);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        handleCommand(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return getCompletions(sender, args);
    }

    private List<String> getCompletions(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        if (!sender.hasPermission("nexoria.spawners.admin")) return list;

        if (args.length == 1) {
            list.add("give");
            list.add("set");
            list.add("reload");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                list.add(p.getName());
            }
        } else if ((args.length == 3 && args[0].equalsIgnoreCase("give")) || (args.length == 2 && args[0].equalsIgnoreCase("set"))) {
            for (String mob : plugin.getConfig().getStringList("allowed-mobs")) {
                list.add(mob.toLowerCase());
            }
        }
        return list;
    }
}
