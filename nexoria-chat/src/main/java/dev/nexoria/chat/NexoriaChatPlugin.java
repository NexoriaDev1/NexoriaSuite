package dev.nexoria.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Entry point for NexoriaChat.
 * Part of the NexoriaSuite server ecosystem.
 */
public final class NexoriaChatPlugin extends JavaPlugin implements TabCompleter {

    private static NexoriaChatPlugin instance;
    private ChatAPI api;
    private ChatCommand chatCommand;

    @Override
    public void onLoad() {
        instance = this;
        this.chatCommand = new ChatCommand(this);
        registerCommand("chat", "Primary command entrypoint for NexoriaChat", List.of("nexoriachat"), this.chatCommand);
    }

    @Override
    public void onEnable() {
        if (instance == null) {
            instance = this;
        }
        if (this.chatCommand == null) {
            this.chatCommand = new ChatCommand(this);
        }

        saveDefaultConfig();

        this.api = new ChatAPI(this);

        // Register events listener
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        // Fallback registration in Bukkit CommandMap if not already bound
        if (getServer().getCommandMap().getCommand("chat") == null) {
            getServer().getCommandMap().register("nexoriachat", this.chatCommand);
        }

        getLogger().log(Level.INFO, "{0} v{1} successfully enabled on {2}", 
                new Object[]{getPluginMeta().getName(), getPluginMeta().getVersion(), getServer().getName()});
    }

    @Override
    public void onDisable() {
        if (this.api != null) {
            this.api.shutdown();
        }
        getLogger().info("NexoriaChat successfully stopped.");
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (this.chatCommand != null) {
            return this.chatCommand.execute(sender, label, args);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (this.chatCommand != null) {
            return this.chatCommand.tabComplete(sender, alias, args);
        }
        return List.of();
    }

    public static NexoriaChatPlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaChat has not been initialized yet");
    }

    public ChatAPI getApi() {
        return api;
    }
}
