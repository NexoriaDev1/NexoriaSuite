package dev.nexoria.economy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class NexoriaEconomyPlugin extends JavaPlugin implements TabCompleter {

    private static NexoriaEconomyPlugin instance;
    private EconomyAPI api;
    private BalanceCommand balanceCommand;
    private PayCommand payCommand;
    private BaltopCommand baltopCommand;
    private EconomyCommand economyCommand;

    @Override
    public void onLoad() {
        instance = this;
        this.balanceCommand = new BalanceCommand(this);
        this.payCommand = new PayCommand(this);
        this.baltopCommand = new BaltopCommand(this);
        this.economyCommand = new EconomyCommand(this);

        registerCommand("balance", "Check player balance", List.of("bal", "money"), this.balanceCommand);
        registerCommand("pay", "Pay another player", List.of(), this.payCommand);
        registerCommand("balancetop", "Top richest players", List.of("baltop"), this.baltopCommand);
        registerCommand("economy", "Economy administration", List.of("eco"), this.economyCommand);
    }

    @Override
    public void onEnable() {
        if (instance == null) {
            instance = this;
        }
        if (this.balanceCommand == null) {
            this.balanceCommand = new BalanceCommand(this);
            this.payCommand = new PayCommand(this);
            this.baltopCommand = new BaltopCommand(this);
            this.economyCommand = new EconomyCommand(this);
        }

        saveDefaultConfig();

        this.api = new EconomyAPI(this);

        getServer().getPluginManager().registerEvents(new EconomyListener(this), this);

        // Fallback registrations in Bukkit CommandMap
        if (getServer().getCommandMap().getCommand("balance") == null) {
            getServer().getCommandMap().register("nexoriaeconomy", this.balanceCommand);
        }
        if (getServer().getCommandMap().getCommand("pay") == null) {
            getServer().getCommandMap().register("nexoriaeconomy", this.payCommand);
        }
        if (getServer().getCommandMap().getCommand("balancetop") == null) {
            getServer().getCommandMap().register("nexoriaeconomy", this.baltopCommand);
        }
        if (getServer().getCommandMap().getCommand("economy") == null) {
            getServer().getCommandMap().register("nexoriaeconomy", this.economyCommand);
        }

        VaultHook.register(this);

        getLogger().log(Level.INFO, "{0} v{1} enabled on {2}", 
                new Object[]{getPluginMeta().getName(), getPluginMeta().getVersion(), getServer().getName()});
    }

    @Override
    public void onDisable() {
        VaultHook.unregister(this);

        if (this.api != null) {
            this.api.shutdown();
        }
        getLogger().info("NexoriaEconomy disabled.");
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase();
        return switch (name) {
            case "pay" -> this.payCommand != null && this.payCommand.execute(sender, label, args);
            case "balancetop", "baltop" -> this.baltopCommand != null && this.baltopCommand.execute(sender, label, args);
            case "economy", "eco" -> this.economyCommand != null && this.economyCommand.execute(sender, label, args);
            default -> this.balanceCommand != null && this.balanceCommand.execute(sender, label, args);
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String name = command.getName().toLowerCase();
        return switch (name) {
            case "pay" -> this.payCommand != null ? this.payCommand.tabComplete(sender, alias, args) : List.of();
            case "economy", "eco" -> this.economyCommand != null ? this.economyCommand.tabComplete(sender, alias, args) : List.of();
            default -> this.balanceCommand != null ? this.balanceCommand.tabComplete(sender, alias, args) : List.of();
        };
    }

    public static NexoriaEconomyPlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaEconomy not initialized");
    }

    public EconomyAPI getApi() {
        return api;
    }
}
