package dev.nexoria.nbt;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class NBTCommand implements BasicCommand, CommandExecutor, TabCompleter {

    private final NexoriaNBTPlugin plugin;

    public NBTCommand(NexoriaNBTPlugin plugin) {
        this.plugin = plugin;
    }

    private String msg(String key) {
        String p = plugin.getConfig().getString("messages.prefix", "&8[&bNBT&8] ");
        String m = plugin.getConfig().getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', p + m);
    }

    private void handleCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexoria.nbt.admin")) {
            sender.sendMessage(msg("no-permission"));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(msg("reloaded"));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only in-game players can view or edit held item PDC tags.");
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(msg("no-item"));
            return;
        }

        if (args.length > 0 && (args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("info"))) {
            Map<String, String> tags = plugin.getNBTManager().getAllTags(item);
            if (tags.isEmpty()) {
                player.sendMessage(msg("no-tags"));
                return;
            }

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.tags-header", "&bCustom PDC Tags:")));
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                player.sendMessage(ChatColor.DARK_AQUA + "- " + ChatColor.YELLOW + entry.getKey() + ChatColor.GRAY + " = " + ChatColor.WHITE + entry.getValue());
            }
            return;
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("set")) {
            String key = args[1];
            String value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            plugin.getNBTManager().setString(item, key, value);
            player.sendMessage(msg("tag-set").replace("{key}", key).replace("{value}", value));
            return;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("remove")) {
            String key = args[1];
            plugin.getNBTManager().remove(item, key);
            player.sendMessage(msg("tag-removed").replace("{key}", key));
            return;
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bNexoriaNBT &7v" + plugin.getPluginMeta().getVersion()));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/nbt view              &7- View PDC tags on held item"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/nbt set <key> <val>   &7- Set string tag on item"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/nbt remove <key>      &7- Remove tag from item"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/nbt reload            &7- Reload configuration"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
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
        if (!sender.hasPermission("nexoria.nbt.admin")) return list;

        if (args.length == 1) {
            list.add("view");
            list.add("set");
            list.add("remove");
            list.add("reload");
        }
        return list;
    }
}
