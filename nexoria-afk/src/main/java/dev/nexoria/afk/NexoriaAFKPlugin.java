package dev.nexoria.afk;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Entry point for NexoriaAFK.
 * Part of the NexoriaSuite server ecosystem.
 */
public final class NexoriaAFKPlugin extends JavaPlugin implements TabCompleter {

    private static NexoriaAFKPlugin instance;
    private AFKAPI api;
    private AFKCommand afkCommand;
    private BukkitTask tickerTask;

    @Override
    public void onLoad() {
        instance = this;
        this.afkCommand = new AFKCommand(this);
        registerCommand("afk", "Toggle your AFK status or inspect server idle players", List.of("away"), this.afkCommand);
    }

    @Override
    public void onEnable() {
        if (instance == null) {
            instance = this;
        }
        if (this.afkCommand == null) {
            this.afkCommand = new AFKCommand(this);
        }

        saveDefaultConfig();

        this.api = new AFKAPI(this);

        // Register events listener
        getServer().getPluginManager().registerEvents(new AFKListener(this), this);

        // Fallback registration in Bukkit CommandMap if not already bound
        if (getServer().getCommandMap().getCommand("afk") == null) {
            getServer().getCommandMap().register("nexoriaafk", this.afkCommand);
        }

        // Periodic ticker for auto-AFK and auto-kick (runs every 5 seconds / 100 ticks)
        this.tickerTask = getServer().getScheduler().runTaskTimer(this, () -> {
            if (this.api != null) {
                this.api.checkAllPlayers();
            }
        }, 100L, 100L);

        getLogger().log(Level.INFO, "{0} v{1} successfully enabled on {2}", 
                new Object[]{getPluginMeta().getName(), getPluginMeta().getVersion(), getServer().getName()});
    }

    @Override
    public void onDisable() {
        if (this.tickerTask != null) {
            this.tickerTask.cancel();
            this.tickerTask = null;
        }

        if (this.api != null) {
            this.api.shutdown();
        }
        getLogger().info("NexoriaAFK successfully stopped.");
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (this.afkCommand != null) {
            return this.afkCommand.execute(sender, label, args);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (this.afkCommand != null) {
            return this.afkCommand.tabComplete(sender, alias, args);
        }
        return List.of();
    }

    public static NexoriaAFKPlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaAFK has not been initialized yet");
    }

    public AFKAPI getApi() {
        return api;
    }
}
