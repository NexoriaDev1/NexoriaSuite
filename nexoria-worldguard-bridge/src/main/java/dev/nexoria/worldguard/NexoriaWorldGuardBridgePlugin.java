package dev.nexoria.worldguard;

import io.papermc.paper.command.brigadier.BasicCommand;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class NexoriaWorldGuardBridgePlugin extends JavaPlugin {

    private static NexoriaWorldGuardBridgePlugin instance;
    private WorldGuardHook hook;
    private WorldGuardBridgeAPI api;

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
                    registerCommand(registrar, registrarClass, "wgbridge", "WorldGuard bridge diagnostics", List.of("worldguardbridge", "wgb"), new WorldGuardBridgeCommand(this));
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

        this.hook = new WorldGuardHook(this);
        this.api = new WorldGuardBridgeAPI(this);

        fallbackRegisterCommands();

        getServer().getPluginManager().registerEvents(new WorldGuardBridgeListener(this), this);

        getLogger().log(Level.INFO, "NexoriaWorldGuardBridge v{0} successfully enabled.", getPluginMeta().getVersion());
    }

    private void fallbackRegisterCommands() {
        try {
            Method getCommandMap = getServer().getClass().getMethod("getCommandMap");
            CommandMap map = (CommandMap) getCommandMap.invoke(getServer());
            if (map != null && map.getCommand("wgbridge") == null) {
                map.register("nexoriaworldguardbridge", new WorldGuardBridgeCommand(this));
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void onDisable() {
        if (api != null) {
            api.shutdown();
        }
        instance = null;
    }

    public static NexoriaWorldGuardBridgePlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaWorldGuardBridge is not initialized");
    }

    public WorldGuardHook getHook() {
        return hook;
    }

    public WorldGuardBridgeAPI getApi() {
        return api;
    }
}
