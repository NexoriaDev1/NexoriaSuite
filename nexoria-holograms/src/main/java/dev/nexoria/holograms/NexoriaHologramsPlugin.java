package dev.nexoria.holograms;

import io.papermc.paper.command.brigadier.BasicCommand;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class NexoriaHologramsPlugin extends JavaPlugin {

    private static NexoriaHologramsPlugin instance;
    private HologramManager hologramManager;
    private HologramsAPI api;

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
                    registerCommand(registrar, registrarClass, "hologram", "Manage floating holograms", List.of("holo", "holograms"), new HologramCommand(this));
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

        this.hologramManager = new HologramManager(this);
        this.api = new HologramsAPI(this);

        fallbackRegisterCommand();

        getServer().getPluginManager().registerEvents(new HologramsListener(this), this);

        getLogger().log(Level.INFO, "NexoriaHolograms v{0} successfully enabled on {1}",
                new Object[]{getPluginMeta().getVersion(), getServer().getName()});
    }

    private void fallbackRegisterCommand() {
        try {
            Method getCommandMap = getServer().getClass().getMethod("getCommandMap");
            CommandMap map = (CommandMap) getCommandMap.invoke(getServer());
            if (map != null && map.getCommand("hologram") == null) {
                HologramCommand handler = new HologramCommand(this);
                BukkitCommand cmd = new BukkitCommand("hologram", "Manage floating holograms", "/hologram", List.of("holo", "holograms")) {
                    @Override
                    public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                        return handler.onCommand(sender, this, label, args);
                    }
                    @Override
                    public List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                        return handler.onTabComplete(sender, this, alias, args);
                    }
                };
                map.register("nexoriaholograms", cmd);
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void onDisable() {
        if (hologramManager != null) {
            hologramManager.despawnAll();
            hologramManager.saveData();
        }
        instance = null;
    }

    public static NexoriaHologramsPlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaHolograms is not active");
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public HologramsAPI getApi() {
        return api;
    }
}
