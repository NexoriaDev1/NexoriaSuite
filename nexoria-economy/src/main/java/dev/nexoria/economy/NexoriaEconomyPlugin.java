package dev.nexoria.economy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Entry point for NexoriaEconomy.
 * Part of the NexoriaSuite server ecosystem.
 */
public final class NexoriaEconomyPlugin extends JavaPlugin implements TabCompleter {

    private static NexoriaEconomyPlugin instance;
    private EconomyAPI api;
    private EconomyCommand economyCommand;

    @Override
    public void onLoad() {
        instance = this;
        this.economyCommand = new EconomyCommand(this);
        // Register Paper Brigadier command lifecycle
        registerCommand("balance", "Primary command entrypoint for NexoriaEconomy", List.of("economy", "eco"), this.economyCommand);
    }

    @Override
    public void onEnable() {
        if (instance == null) {
            instance = this;
        }
        if (this.economyCommand == null) {
            this.economyCommand = new EconomyCommand(this);
        }

        saveDefaultConfig();

        this.api = new EconomyAPI(this);
        
        // Register events listener
        getServer().getPluginManager().registerEvents(new EconomyListener(this), this);

        // Fallback registration in Bukkit CommandMap if not already bound
        if (getServer().getCommandMap().getCommand("balance") == null) {
            getServer().getCommandMap().register("nexoriaeconomy", this.economyCommand);
        }

        // Register Vault Economy provider if Vault is present
        VaultHook.register(this);

        getLogger().log(Level.INFO, "{0} v{1} successfully enabled on {2}", 
                new Object[]{getPluginMeta().getName(), getPluginMeta().getVersion(), getServer().getName()});
    }

    @Override
    public void onDisable() {
        VaultHook.unregister(this);

        if (this.api != null) {
            this.api.shutdown();
        }
        getLogger().info("NexoriaEconomy successfully stopped.");
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (this.economyCommand != null) {
            return this.economyCommand.execute(sender, label, args);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (this.economyCommand != null) {
            return this.economyCommand.tabComplete(sender, alias, args);
        }
        return List.of();
    }

    public static NexoriaEconomyPlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaEconomy has not been initialized yet");
    }

    public EconomyAPI getApi() {
        return api;
    }
}
