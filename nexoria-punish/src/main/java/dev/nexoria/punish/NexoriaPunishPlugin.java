package dev.nexoria.punish;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Entry point for NexoriaPunish.
 * Part of the NexoriaSuite server ecosystem.
 */
public final class NexoriaPunishPlugin extends JavaPlugin implements TabCompleter {

    private static NexoriaPunishPlugin instance;
    private PunishAPI api;
    private PunishCommand punishCommand;

    @Override
    public void onLoad() {
        instance = this;
        this.punishCommand = new PunishCommand(this);
        registerCommand("punish", "Primary command entrypoint for NexoriaPunish", List.of("nexoriapunish"), this.punishCommand);
        registerCommand("ban", "Ban a player", List.of(), this.punishCommand);
        registerCommand("unban", "Unban a player", List.of(), this.punishCommand);
        registerCommand("mute", "Mute a player", List.of(), this.punishCommand);
        registerCommand("unmute", "Unmute a player", List.of(), this.punishCommand);
        registerCommand("kick", "Kick a player", List.of(), this.punishCommand);
    }

    @Override
    public void onEnable() {
        if (instance == null) {
            instance = this;
        }
        if (this.punishCommand == null) {
            this.punishCommand = new PunishCommand(this);
        }

        saveDefaultConfig();

        this.api = new PunishAPI(this);

        // Register events listener
        getServer().getPluginManager().registerEvents(new PunishListener(this), this);

        // Fallback registration in Bukkit CommandMap if not already bound
        if (getServer().getCommandMap().getCommand("punish") == null) {
            getServer().getCommandMap().register("nexoriapunish", this.punishCommand);
        }
        if (getServer().getCommandMap().getCommand("ban") == null) {
            getServer().getCommandMap().register("nexoriapunish", this.punishCommand);
        }
        if (getServer().getCommandMap().getCommand("unban") == null) {
            getServer().getCommandMap().register("nexoriapunish", this.punishCommand);
        }
        if (getServer().getCommandMap().getCommand("mute") == null) {
            getServer().getCommandMap().register("nexoriapunish", this.punishCommand);
        }
        if (getServer().getCommandMap().getCommand("unmute") == null) {
            getServer().getCommandMap().register("nexoriapunish", this.punishCommand);
        }
        if (getServer().getCommandMap().getCommand("kick") == null) {
            getServer().getCommandMap().register("nexoriapunish", this.punishCommand);
        }

        getLogger().log(Level.INFO, "{0} v{1} successfully enabled on {2}", 
                new Object[]{getPluginMeta().getName(), getPluginMeta().getVersion(), getServer().getName()});
    }

    @Override
    public void onDisable() {
        if (this.api != null) {
            this.api.shutdown();
        }
        getLogger().info("NexoriaPunish successfully stopped.");
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (this.punishCommand != null) {
            return this.punishCommand.handleExecution(sender, command.getName(), args);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (this.punishCommand != null) {
            return this.punishCommand.tabComplete(sender, alias, args);
        }
        return List.of();
    }

    public static NexoriaPunishPlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaPunish has not been initialized yet");
    }

    public PunishAPI getApi() {
        return api;
    }
}
