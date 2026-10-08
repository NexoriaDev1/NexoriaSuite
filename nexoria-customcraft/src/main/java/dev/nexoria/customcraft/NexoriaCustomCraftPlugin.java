package dev.nexoria.customcraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Entry point for NexoriaCustomCraft.
 * Part of the NexoriaSuite server ecosystem.
 */
public final class NexoriaCustomCraftPlugin extends JavaPlugin implements TabCompleter {

    private static NexoriaCustomCraftPlugin instance;
    private CustomCraftAPI api;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.api = new CustomCraftAPI(this);
        
        // Register events listener
        getServer().getPluginManager().registerEvents(new CustomCraftListener(this), this);

        getLogger().log(Level.INFO, "{0} v{1} successfully enabled on {2}", 
                new Object[]{getPluginMeta().getName(), getPluginMeta().getVersion(), getServer().getName()});
    }

    @Override
    public void onDisable() {
        if (this.api != null) {
            this.api.shutdown();
        }
        getLogger().info("NexoriaCustomCraft successfully stopped.");
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("nexoria.customcraft.admin")) {
                sender.sendMessage("§cYou lack permission: nexoria.customcraft.admin");
                return true;
            }
            reloadConfig();
            sender.sendMessage("§a[NexoriaCustomCraft] Configuration successfully reloaded.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("status")) {
            var status = api.getStatus();
            sender.sendMessage("§b[NexoriaCustomCraft] §7Status: " + (status.active() ? "§aOperational" : "§cSuspended"));
            return true;
        }

        sender.sendMessage("§8[§bNexoriaCustomCraft§8] §7Running version §f" + getPluginMeta().getVersion() + " §7on §bJava 25§7.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("status", "reload", "help");
        }
        return List.of();
    }

    public static NexoriaCustomCraftPlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaCustomCraft has not been initialized yet");
    }

    public CustomCraftAPI getApi() {
        return api;
    }
}
