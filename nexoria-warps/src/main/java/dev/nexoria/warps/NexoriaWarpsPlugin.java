package dev.nexoria.warps;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Entry point for NexoriaWarps.
 * Part of the NexoriaSuite server ecosystem.
 */
public final class NexoriaWarpsPlugin extends JavaPlugin implements TabCompleter {

    private static NexoriaWarpsPlugin instance;
    private WarpsAPI api;
    private WarpCommand warpCommand;
    private SetWarpCommand setWarpCommand;
    private DelWarpCommand delWarpCommand;

    @Override
    public void onLoad() {
        instance = this;
        this.warpCommand = new WarpCommand(this);
        this.setWarpCommand = new SetWarpCommand(this);
        this.delWarpCommand = new DelWarpCommand(this);

        registerCommand("warp", "Teleport to server warps", List.of("warps"), this.warpCommand);
        registerCommand("setwarp", "Create a new server warp", List.of(), this.setWarpCommand);
        registerCommand("delwarp", "Delete an existing server warp", List.of(), this.delWarpCommand);
    }

    @Override
    public void onEnable() {
        if (instance == null) {
            instance = this;
        }
        if (this.warpCommand == null) {
            this.warpCommand = new WarpCommand(this);
            this.setWarpCommand = new SetWarpCommand(this);
            this.delWarpCommand = new DelWarpCommand(this);
        }

        saveDefaultConfig();

        this.api = new WarpsAPI(this);

        // Register events listener
        getServer().getPluginManager().registerEvents(new WarpsListener(this), this);

        // Fallback registrations in Bukkit CommandMap
        if (getServer().getCommandMap().getCommand("warp") == null) {
            getServer().getCommandMap().register("nexoriawarps", this.warpCommand);
        }
        if (getServer().getCommandMap().getCommand("setwarp") == null) {
            getServer().getCommandMap().register("nexoriawarps", this.setWarpCommand);
        }
        if (getServer().getCommandMap().getCommand("delwarp") == null) {
            getServer().getCommandMap().register("nexoriawarps", this.delWarpCommand);
        }

        getLogger().log(Level.INFO, "{0} v{1} successfully enabled on {2}", 
                new Object[]{getPluginMeta().getName(), getPluginMeta().getVersion(), getServer().getName()});
    }

    @Override
    public void onDisable() {
        if (this.api != null) {
            this.api.shutdown();
        }
        getLogger().info("NexoriaWarps successfully stopped.");
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("setwarp")) {
            return this.setWarpCommand.execute(sender, label, args);
        }
        if (command.getName().equalsIgnoreCase("delwarp")) {
            return this.delWarpCommand.execute(sender, label, args);
        }
        if (this.warpCommand != null) {
            return this.warpCommand.execute(sender, label, args);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("delwarp")) {
            return this.delWarpCommand.tabComplete(sender, alias, args);
        }
        if (this.warpCommand != null) {
            return this.warpCommand.tabComplete(sender, alias, args);
        }
        return List.of();
    }

    public static NexoriaWarpsPlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaWarps has not been initialized yet");
    }

    public WarpsAPI getApi() {
        return api;
    }
}
