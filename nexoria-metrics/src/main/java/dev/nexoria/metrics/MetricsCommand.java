package dev.nexoria.metrics;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MetricsCommand implements BasicCommand, CommandExecutor, TabCompleter {

    private final NexoriaMetricsPlugin plugin;

    public MetricsCommand(NexoriaMetricsPlugin plugin) {
        this.plugin = plugin;
    }

    private String msg(String key) {
        String p = plugin.getConfig().getString("messages.prefix", "&8[&aMetrics&8] ");
        String m = plugin.getConfig().getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', p + m);
    }

    private void handleCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexoria.metrics.use")) {
            sender.sendMessage(msg("no-permission"));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("nexoria.metrics.admin")) {
                sender.sendMessage(msg("no-permission"));
                return;
            }
            plugin.reloadConfig();
            sender.sendMessage(msg("reloaded"));
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("gc")) {
            if (!sender.hasPermission("nexoria.metrics.admin")) {
                sender.sendMessage(msg("no-permission"));
                return;
            }
            plugin.getMetricsManager().runGC();
            sender.sendMessage(msg("gc-executed"));
            return;
        }

        MetricsSnapshot s = plugin.getMetricsManager().getSnapshot();
        MetricsManager mgr = plugin.getMetricsManager();

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aNexoriaMetrics &7- Live Diagnostics"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7TPS: " +
                mgr.formatTPS(s.tps()[0]) + " &7(1m), " +
                mgr.formatTPS(s.tps()[1]) + " &7(5m), " +
                mgr.formatTPS(s.tps()[2]) + " &7(15m)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7Tick Duration (MSPT): " + mgr.formatMSPT(s.mspt())));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7Memory: &e" + s.usedMemoryMb() + "MB &7/ &f" + s.maxMemoryMb() + "MB"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7Active Threads: &b" + s.activeThreads()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', " &7Server Uptime: &f" + mgr.formatUptime(s.uptimeMillis())));
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
        if (args.length == 1 && sender.hasPermission("nexoria.metrics.admin")) {
            list.add("gc");
            list.add("reload");
        }
        return list;
    }
}
