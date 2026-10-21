package dev.nexoria.antigrief;

import io.papermc.paper.command.brigadier.BasicCommand;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class NexoriaAntiGriefPlugin extends JavaPlugin {

    private static NexoriaAntiGriefPlugin instance;
    private AntiGriefManager antiGriefManager;
    private AntiGriefAPI api;

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
                    registerCommand(registrar, registrarClass, "antigrief", "Manage server protection settings", List.of("ag"), new AntiGriefCommand(this));
                }
                return null;
            };

            Object handlerProxy = java.lang.reflect.Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class<?>[]{handlerClass},
                    invocationHandler);

            registerMethod.invoke(lifecycleManager, commandEvent, handlerProxy);
        } catch (Throwable ignored) {
            // Fallback in onEnable
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

        this.antiGriefManager = new AntiGriefManager(this);
        this.api = new AntiGriefAPI(this);

        fallbackRegisterCommand();

        getServer().getPluginManager().registerEvents(new AntiGriefListener(this), this);

        getLogger().log(Level.INFO, "NexoriaAntiGrief v{0} successfully enabled on {1}",
                new Object[]{getPluginMeta().getVersion(), getServer().getName()});
    }

    private void fallbackRegisterCommand() {
        try {
            Method getCommandMap = getServer().getClass().getMethod("getCommandMap");
            CommandMap map = (CommandMap) getCommandMap.invoke(getServer());
            if (map != null && map.getCommand("antigrief") == null) {
                AntiGriefCommand handler = new AntiGriefCommand(this);
                BukkitCommand cmd = new BukkitCommand("antigrief", "Manage server protection settings", "/antigrief", List.of("ag")) {
                    @Override
                    public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                        return handler.onCommand(sender, this, label, args);
                    }
                    @Override
                    public List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                        return handler.onTabComplete(sender, this, alias, args);
                    }
                };
                map.register("nexoriaantigrief", cmd);
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static NexoriaAntiGriefPlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaAntiGrief is not active");
    }

    public AntiGriefManager getAntiGriefManager() {
        return antiGriefManager;
    }

    public AntiGriefAPI getApi() {
        return api;
    }
}
