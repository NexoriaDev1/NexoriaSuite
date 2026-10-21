package dev.nexoria.scoreboard;

import io.papermc.paper.command.brigadier.BasicCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class NexoriaScoreboardPlugin extends JavaPlugin {

    private static NexoriaScoreboardPlugin instance;
    private ScoreboardManager scoreboardManager;
    private ScoreboardAPI api;
    private BukkitTask updateTask;

    @Override
    public void onLoad() {
        registerBrigadier();
    }

    private void registerBrigadier() {
        try {
            Method getLifecycleManagerMethod = getClass().getMethod("getLifecycleManager");
            Object lifecycleManager = getLifecycleManagerMethod.invoke(this);
            Class<?> registrarClass = Class.forName("io.papermc.paper.command.brigadier.Commands");
            Class<?> handlerClass = Class.forName("io.papermc.paper.plugin.lifecycle.event.types.LifecycleEventHandler");

            Method registerMethod = lifecycleManager.getClass().getMethod("registerEventHandler",
                    Class.forName("io.papermc.paper.plugin.lifecycle.event.LifecycleEvent"),
                    handlerClass);

            Class<?> eventTypesClass = Class.forName("io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents");
            Object commandEvent = eventTypesClass.getField("COMMANDS").get(null);

            java.lang.reflect.InvocationHandler invocationHandler = (proxy, method, methodArgs) -> {
                if (method.getName().equals("run")) {
                    Object event = methodArgs[0];
                    Method getRegistrar = event.getClass().getMethod("registrar");
                    Object registrar = getRegistrar.invoke(event);
                    registerCommand(registrar, registrarClass, "scoreboard", "Toggle or manage scoreboard", List.of("sb", "sidebar"), new ScoreboardCommand(this));
                }
                return null;
            };

            Object handlerProxy = java.lang.reflect.Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class<?>[]{handlerClass},
                    invocationHandler);

            registerMethod.invoke(lifecycleManager, commandEvent, handlerProxy);
        } catch (Throwable ignored) {
            // Fallback will run in onEnable
        }
    }

    private void registerCommand(Object registrar, Class<?> registrarClass, String name, String desc, List<String> aliases, BasicCommand cmd) {
        try {
            Method reg = registrarClass.getMethod("register", String.class, String.class, List.class, BasicCommand.class);
            reg.invoke(registrar, name, desc, aliases, cmd);
        } catch (Throwable t) {
            try {
                Method reg = registrarClass.getMethod("register", String.class, BasicCommand.class);
                reg.invoke(registrar, name, cmd);
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.scoreboardManager = new ScoreboardManager(this);
        this.api = new ScoreboardAPI(this);

        fallbackRegisterCommand();

        getServer().getPluginManager().registerEvents(new ScoreboardListener(this), this);

        // Schedule periodic update
        long interval = Math.max(5L, getConfig().getLong("settings.update-interval", 20L));
        this.updateTask = getServer().getScheduler().runTaskTimer(this, () -> {
            if (scoreboardManager != null) {
                scoreboardManager.updateAll();
            }
        }, 20L, interval);

        // Create scoreboards for online players (e.g. on /reload)
        for (var player : Bukkit.getOnlinePlayers()) {
            scoreboardManager.createScoreboard(player);
        }

        getLogger().log(Level.INFO, "NexoriaScoreboard v{0} successfully initialized.", getPluginMeta().getVersion());
    }

    private void fallbackRegisterCommand() {
        try {
            Method getCommandMap = getServer().getClass().getMethod("getCommandMap");
            CommandMap map = (CommandMap) getCommandMap.invoke(getServer());
            if (map != null && map.getCommand("scoreboard") == null) {
                ScoreboardCommand handler = new ScoreboardCommand(this);
                BukkitCommand cmd = new BukkitCommand("scoreboard", "Toggle or manage scoreboard", "/scoreboard [toggle|reload]", List.of("sb", "sidebar")) {
                    @Override
                    public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                        return handler.onCommand(sender, this, label, args);
                    }
                    @Override
                    public List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                        return handler.onTabComplete(sender, this, alias, args);
                    }
                };
                map.register("nexoriascoreboard", cmd);
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void onDisable() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        if (scoreboardManager != null) {
            scoreboardManager.shutdown();
        }
        instance = null;
    }

    public static NexoriaScoreboardPlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaScoreboard is not active");
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public ScoreboardAPI getApi() {
        return api;
    }
}
