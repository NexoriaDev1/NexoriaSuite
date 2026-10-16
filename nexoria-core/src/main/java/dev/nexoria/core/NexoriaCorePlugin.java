package dev.nexoria.core;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Entry point for NexoriaCore.
 * Part of the NexoriaSuite server ecosystem.
 */
public final class NexoriaCorePlugin extends JavaPlugin implements TabCompleter {

    private static NexoriaCorePlugin instance;
    private CoreAPI api;
    private CoreCommand coreCommand;

    @Override
    public void onLoad() {
        instance = this;
        this.coreCommand = new CoreCommand(this);
        // Register Paper Brigadier command lifecycle
        registerCommand("core", "Primary command entrypoint for NexoriaCore", List.of("nexoriacore"), this.coreCommand);
    }

    @Override
    public void onEnable() {
        if (instance == null) {
            instance = this;
        }
        if (this.coreCommand == null) {
            this.coreCommand = new CoreCommand(this);
        }
        
        saveDefaultConfig();

        this.api = new CoreAPI(this);
        
        // Register events listener
        getServer().getPluginManager().registerEvents(new CoreListener(this), this);

        // Fallback registration in Bukkit CommandMap if not already bound
        if (getServer().getCommandMap().getCommand("core") == null) {
            getServer().getCommandMap().register("nexoriacore", this.coreCommand);
        }

        getLogger().log(Level.INFO, "{0} v{1} successfully enabled on {2}", 
                new Object[]{getPluginMeta().getName(), getPluginMeta().getVersion(), getServer().getName()});
    }

    @Override
    public void onDisable() {
        if (this.api != null) {
            this.api.shutdown();
        }
        getLogger().info("NexoriaCore successfully stopped.");
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (this.coreCommand != null) {
            return this.coreCommand.execute(sender, label, args);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (this.coreCommand != null) {
            return this.coreCommand.tabComplete(sender, alias, args);
        }
        return List.of();
    }

    public static NexoriaCorePlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaCore has not been initialized yet");
    }

    public CoreAPI getApi() {
        return api;
    }
}
