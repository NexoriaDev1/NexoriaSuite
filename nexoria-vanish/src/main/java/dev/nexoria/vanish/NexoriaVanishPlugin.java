package dev.nexoria.vanish;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Entry point for NexoriaVanish.
 * Part of the NexoriaSuite server ecosystem.
 */
public final class NexoriaVanishPlugin extends JavaPlugin implements TabCompleter {

    private static NexoriaVanishPlugin instance;
    private VanishAPI api;
    private VanishCommand vanishCommand;

    @Override
    public void onLoad() {
        instance = this;
        this.vanishCommand = new VanishCommand(this);
        registerCommand("vanish", "Toggle stealth vanish mode", List.of("v"), this.vanishCommand);
    }

    @Override
    public void onEnable() {
        if (instance == null) {
            instance = this;
        }
        if (this.vanishCommand == null) {
            this.vanishCommand = new VanishCommand(this);
        }

        saveDefaultConfig();

        this.api = new VanishAPI(this);

        // Register events listener
        getServer().getPluginManager().registerEvents(new VanishListener(this), this);

        // Fallback registration in Bukkit CommandMap if not already bound
        if (getServer().getCommandMap().getCommand("vanish") == null) {
            getServer().getCommandMap().register("nexoriavanish", this.vanishCommand);
        }

        getLogger().log(Level.INFO, "{0} v{1} successfully enabled on {2}", 
                new Object[]{getPluginMeta().getName(), getPluginMeta().getVersion(), getServer().getName()});
    }

    @Override
    public void onDisable() {
        if (this.api != null) {
            this.api.shutdown();
        }
        getLogger().info("NexoriaVanish successfully stopped.");
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (this.vanishCommand != null) {
            return this.vanishCommand.execute(sender, label, args);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (this.vanishCommand != null) {
            return this.vanishCommand.tabComplete(sender, alias, args);
        }
        return List.of();
    }

    public static NexoriaVanishPlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaVanish has not been initialized yet");
    }

    public VanishAPI getApi() {
        return api;
    }
}
