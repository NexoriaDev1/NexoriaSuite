package dev.nexoria.punish;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class NexoriaPunishPlugin extends JavaPlugin implements TabCompleter {

    private static NexoriaPunishPlugin instance;
    private PunishAPI api;

    private PunishCommand punishCommand;
    private PunishCommand banCommand;
    private PunishCommand unbanCommand;
    private PunishCommand muteCommand;
    private PunishCommand unmuteCommand;
    private PunishCommand kickCommand;

    @Override
    public void onLoad() {
        instance = this;
        initCommands();

        registerCommand("punish", "Primary command entrypoint for NexoriaPunish", List.of("nexoriapunish"), this.punishCommand);
        registerCommand("ban", "Permanently ban a player", List.of(), this.banCommand);
        registerCommand("unban", "Unban a player", List.of(), this.unbanCommand);
        registerCommand("mute", "Mute a player", List.of(), this.muteCommand);
        registerCommand("unmute", "Unmute a player", List.of(), this.unmuteCommand);
        registerCommand("kick", "Kick a player", List.of(), this.kickCommand);
    }

    private void initCommands() {
        if (this.punishCommand == null) {
            this.punishCommand = new PunishCommand(this, "punish", "Primary command entrypoint for NexoriaPunish", "/punish", List.of("nexoriapunish"), "punish");
            this.banCommand = new PunishCommand(this, "ban", "Permanently ban a player", "/ban <player> [reason]", List.of(), "ban");
            this.unbanCommand = new PunishCommand(this, "unban", "Unban a player", "/unban <player>", List.of(), "unban");
            this.muteCommand = new PunishCommand(this, "mute", "Mute a player", "/mute <player> [reason]", List.of(), "mute");
            this.unmuteCommand = new PunishCommand(this, "unmute", "Unmute a player", "/unmute <player>", List.of(), "unmute");
            this.kickCommand = new PunishCommand(this, "kick", "Kick a player", "/kick <player> [reason]", List.of(), "kick");
        }
    }

    @Override
    public void onEnable() {
        if (instance == null) {
            instance = this;
        }
        initCommands();

        saveDefaultConfig();

        this.api = new PunishAPI(this);

        getServer().getPluginManager().registerEvents(new PunishListener(this), this);

        // Fallback registrations in Bukkit CommandMap
        if (getServer().getCommandMap().getCommand("punish") == null) {
            getServer().getCommandMap().register("nexoriapunish", this.punishCommand);
        }
        if (getServer().getCommandMap().getCommand("ban") == null) {
            getServer().getCommandMap().register("nexoriapunish", this.banCommand);
        }
        if (getServer().getCommandMap().getCommand("unban") == null) {
            getServer().getCommandMap().register("nexoriapunish", this.unbanCommand);
        }
        if (getServer().getCommandMap().getCommand("mute") == null) {
            getServer().getCommandMap().register("nexoriapunish", this.muteCommand);
        }
        if (getServer().getCommandMap().getCommand("unmute") == null) {
            getServer().getCommandMap().register("nexoriapunish", this.unmuteCommand);
        }
        if (getServer().getCommandMap().getCommand("kick") == null) {
            getServer().getCommandMap().register("nexoriapunish", this.kickCommand);
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
        String name = command.getName().toLowerCase();
        return switch (name) {
            case "ban" -> this.banCommand != null && this.banCommand.handleExecution(sender, "ban", args);
            case "unban" -> this.unbanCommand != null && this.unbanCommand.handleExecution(sender, "unban", args);
            case "mute" -> this.muteCommand != null && this.muteCommand.handleExecution(sender, "mute", args);
            case "unmute" -> this.unmuteCommand != null && this.unmuteCommand.handleExecution(sender, "unmute", args);
            case "kick" -> this.kickCommand != null && this.kickCommand.handleExecution(sender, "kick", args);
            default -> this.punishCommand != null && this.punishCommand.handleExecution(sender, "punish", args);
        };
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
