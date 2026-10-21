package dev.nexoria.claims;

import io.papermc.paper.command.brigadier.BasicCommand;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class NexoriaClaimsPlugin extends JavaPlugin {

    private static NexoriaClaimsPlugin instance;
    private ClaimManager claimManager;
    private ClaimsAPI api;

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
                    registerCommand(registrar, registrarClass, "claim", "Claim current chunk", List.of("landclaim"), new ClaimsCommand(this, "claim", "claim", List.of("landclaim")));
                    registerCommand(registrar, registrarClass, "unclaim", "Unclaim current chunk", List.of(), new ClaimsCommand(this, "unclaim", "unclaim", List.of()));
                    registerCommand(registrar, registrarClass, "trust", "Trust player in claims", List.of(), new ClaimsCommand(this, "trust", "trust", List.of()));
                    registerCommand(registrar, registrarClass, "untrust", "Untrust player in claims", List.of(), new ClaimsCommand(this, "untrust", "untrust", List.of()));
                    registerCommand(registrar, registrarClass, "claimadmin", "Claims administration", List.of("cadmin"), new ClaimsCommand(this, "claimadmin", "claimadmin", List.of("cadmin")));
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

        this.claimManager = new ClaimManager(this);
        this.api = new ClaimsAPI(this);

        fallbackRegisterCommands();

        getServer().getPluginManager().registerEvents(new ClaimsListener(this), this);

        getLogger().log(Level.INFO, "NexoriaClaims v{0} successfully enabled.", getPluginMeta().getVersion());
    }

    private void fallbackRegisterCommands() {
        try {
            Method getCommandMap = getServer().getClass().getMethod("getCommandMap");
            CommandMap map = (CommandMap) getCommandMap.invoke(getServer());
            if (map != null) {
                if (map.getCommand("claim") == null) {
                    map.register("nexoriaclaims", new ClaimsCommand(this, "claim", "claim", List.of("landclaim")));
                }
                if (map.getCommand("unclaim") == null) {
                    map.register("nexoriaclaims", new ClaimsCommand(this, "unclaim", "unclaim", List.of()));
                }
                if (map.getCommand("trust") == null) {
                    map.register("nexoriaclaims", new ClaimsCommand(this, "trust", "trust", List.of()));
                }
                if (map.getCommand("untrust") == null) {
                    map.register("nexoriaclaims", new ClaimsCommand(this, "untrust", "untrust", List.of()));
                }
                if (map.getCommand("claimadmin") == null) {
                    map.register("nexoriaclaims", new ClaimsCommand(this, "claimadmin", "claimadmin", List.of("cadmin")));
                }
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void onDisable() {
        if (claimManager != null) {
            claimManager.saveClaims();
        }
        if (api != null) {
            api.shutdown();
        }
        instance = null;
    }

    public static NexoriaClaimsPlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaClaims is not initialized");
    }

    public ClaimManager getClaimManager() {
        return claimManager;
    }

    public ClaimsAPI getApi() {
        return api;
    }
}
