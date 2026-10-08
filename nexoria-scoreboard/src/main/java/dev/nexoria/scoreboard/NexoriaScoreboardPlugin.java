package dev.nexoria.scoreboard;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Entry point for NexoriaScoreboard.
 * Part of the NexoriaSuite server ecosystem.
 */
public final class NexoriaScoreboardPlugin extends JavaPlugin implements TabCompleter {

    private static NexoriaScoreboardPlugin instance;
    private ScoreboardAPI api;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.api = new ScoreboardAPI(this);
        
        // Register events listener
        getServer().getPluginManager().registerEvents(new ScoreboardListener(this), this);

        getLogger().log(Level.INFO, "{0} v{1} successfully enabled on {2}", 
                new Object[]{getPluginMeta().getName(), getPluginMeta().getVersion(), getServer().getName()});
    }

    @Override
    public void onDisable() {
        if (this.api != null) {
            this.api.shutdown();
        }
        getLogger().info("NexoriaScoreboard successfully stopped.");
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("nexoria.scoreboard.admin")) {
                sender.sendMessage("§cYou lack permission: nexoria.scoreboard.admin");
                return true;
            }
            reloadConfig();
            sender.sendMessage("§a[NexoriaScoreboard] Configuration successfully reloaded.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("status")) {
            var status = api.getStatus();
            sender.sendMessage("§b[NexoriaScoreboard] §7Status: " + (status.active() ? "§aOperational" : "§cSuspended"));
            return true;
        }

        sender.sendMessage("§8[§bNexoriaScoreboard§8] §7Running version §f" + getPluginMeta().getVersion() + " §7on §bJava 25§7.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("status", "reload", "help");
        }
        return List.of();
    }

    public static NexoriaScoreboardPlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaScoreboard has not been initialized yet");
    }

    public ScoreboardAPI getApi() {
        return api;
    }
}
