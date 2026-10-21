package dev.nexoria.holograms;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class HologramCommand implements BasicCommand, CommandExecutor, TabCompleter {

    private final NexoriaHologramsPlugin plugin;

    public HologramCommand(NexoriaHologramsPlugin plugin) {
        this.plugin = plugin;
    }

    private String msg(String key) {
        String p = plugin.getConfig().getString("messages.prefix", "&8[&eHolo&8] ");
        String m = plugin.getConfig().getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', p + m);
    }

    private void handleCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexoria.holograms.admin")) {
            sender.sendMessage(msg("no-permission"));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getHologramManager().reload();
            sender.sendMessage(msg("reloaded"));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
            Map<String, Hologram> all = plugin.getHologramManager().getHolograms();
            if (all.isEmpty()) {
                sender.sendMessage(msg("no-holograms"));
                return;
            }

            sender.sendMessage(ChatColor.GOLD + "Registered Holograms (" + all.size() + "):");
            for (Hologram h : all.values()) {
                sender.sendMessage(ChatColor.YELLOW + "- " + h.getName() + " (" + h.getLines().size() + " lines) at " +
                        h.getLocation().getWorld().getName() + " " + (int)h.getLocation().getX() + "," + (int)h.getLocation().getY() + "," + (int)h.getLocation().getZ());
            }
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only in-game players can manage hologram positions.");
            return;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("create")) {
            String name = args[1];
            boolean created = plugin.getHologramManager().createHologram(name, player.getLocation().add(0, 1.2, 0));
            if (!created) {
                player.sendMessage(msg("already-exists").replace("{name}", name));
                return;
            }
            player.sendMessage(msg("created").replace("{name}", name));
            return;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("delete")) {
            String name = args[1];
            boolean deleted = plugin.getHologramManager().deleteHologram(name);
            if (!deleted) {
                player.sendMessage(msg("not-found").replace("{name}", name));
                return;
            }
            player.sendMessage(msg("deleted").replace("{name}", name));
            return;
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("addline")) {
            String name = args[1];
            String text = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
            boolean success = plugin.getHologramManager().addLine(name, text);
            if (!success) {
                player.sendMessage(msg("not-found").replace("{name}", name));
                return;
            }
            player.sendMessage(msg("line-added").replace("{name}", name));
            return;
        }

        if (args.length >= 4 && args[0].equalsIgnoreCase("setline")) {
            String name = args[1];
            try {
                int index = Integer.parseInt(args[2]);
                String text = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
                boolean success = plugin.getHologramManager().setLine(name, index, text);
                if (!success) {
                    player.sendMessage(msg("not-found").replace("{name}", name));
                    return;
                }
                player.sendMessage(msg("line-set").replace("{name}", name).replace("{index}", String.valueOf(index)));
                return;
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid line index: " + args[2]);
                return;
            }
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("movehere")) {
            String name = args[1];
            boolean success = plugin.getHologramManager().moveHologram(name, player.getLocation().add(0, 1.2, 0));
            if (!success) {
                player.sendMessage(msg("not-found").replace("{name}", name));
                return;
            }
            player.sendMessage(msg("moved").replace("{name}", name));
            return;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eNexoriaHolograms &7v" + plugin.getPluginMeta().getVersion()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/holo create <name>             &7- Create at your location"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/holo delete <name>             &7- Delete hologram"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/holo addline <name> <text>     &7- Add line of text"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/holo setline <name> <i> <text> &7- Edit line of text"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/holo movehere <name>           &7- Move to your location"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/holo list                      &7- List all holograms"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/holo reload                    &7- Reload config & data"));
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
        if (!sender.hasPermission("nexoria.holograms.admin")) return list;

        if (args.length == 1) {
            list.add("create");
            list.add("delete");
            list.add("addline");
            list.add("setline");
            list.add("movehere");
            list.add("list");
            list.add("reload");
        } else if (args.length == 2 && !args[0].equalsIgnoreCase("create") && !args[0].equalsIgnoreCase("list") && !args[0].equalsIgnoreCase("reload")) {
            list.addAll(plugin.getHologramManager().getHolograms().keySet());
        }
        return list;
    }
}
