package dev.nexoria.rtp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Entry point for NexoriaRTP.
 * Part of the NexoriaSuite server ecosystem.
 */
public final class NexoriaRTPPlugin extends JavaPlugin implements TabCompleter {

    private static NexoriaRTPPlugin instance;
    private RTPAPI api;
    private RTPCommand rtpCommand;

    @Override
    public void onLoad() {
        instance = this;
        this.rtpCommand = new RTPCommand(this);
        registerCommand("rtp", "Teleport randomly into the wilderness", List.of("wild", "wilderness"), this.rtpCommand);
    }

    @Override
    public void onEnable() {
        if (instance == null) {
            instance = this;
        }
        if (this.rtpCommand == null) {
            this.rtpCommand = new RTPCommand(this);
        }

        saveDefaultConfig();

        this.api = new RTPAPI(this);

        // Register events listener
        getServer().getPluginManager().registerEvents(new RTPListener(this), this);

        // Fallback registration in Bukkit CommandMap if not already bound
        if (getServer().getCommandMap().getCommand("rtp") == null) {
            getServer().getCommandMap().register("nexoriartp", this.rtpCommand);
        }

        getLogger().log(Level.INFO, "{0} v{1} successfully enabled on {2}", 
                new Object[]{getPluginMeta().getName(), getPluginMeta().getVersion(), getServer().getName()});
    }

    @Override
    public void onDisable() {
        if (this.api != null) {
            this.api.shutdown();
        }
        getLogger().info("NexoriaRTP successfully stopped.");
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (this.rtpCommand != null) {
            return this.rtpCommand.execute(sender, label, args);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (this.rtpCommand != null) {
            return this.rtpCommand.tabComplete(sender, alias, args);
        }
        return List.of();
    }

    public static NexoriaRTPPlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaRTP has not been initialized yet");
    }

    public RTPAPI getApi() {
        return api;
    }
}
