package dev.nexoria.shops;

import io.papermc.paper.command.brigadier.BasicCommand;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class NexoriaShopsPlugin extends JavaPlugin {

    private static NexoriaShopsPlugin instance;
    private ShopManager shopManager;
    private ShopsAPI api;

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
                    registerCommand(registrar, registrarClass, "shop", "Open the server shop GUI", List.of(), new ShopsCommand(this, false));
                    registerCommand(registrar, registrarClass, "sell", "Sell items from inventory", List.of(), new ShopsCommand(this, true));
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

        this.shopManager = new ShopManager(this);
        this.api = new ShopsAPI(this);

        fallbackRegisterCommands();

        getServer().getPluginManager().registerEvents(new ShopsListener(this), this);

        getLogger().log(Level.INFO, "NexoriaShops v{0} successfully enabled.", getPluginMeta().getVersion());
    }

    private void fallbackRegisterCommands() {
        try {
            Method getCommandMap = getServer().getClass().getMethod("getCommandMap");
            CommandMap map = (CommandMap) getCommandMap.invoke(getServer());
            if (map != null) {
                if (map.getCommand("shop") == null) {
                    ShopsCommand handler = new ShopsCommand(this, false);
                    BukkitCommand cmd = new BukkitCommand("shop", "Open shop GUI", "/shop", List.of()) {
                        @Override
                        public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                            return handler.onCommand(sender, this, label, args);
                        }
                        @Override
                        public List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                            return handler.onTabComplete(sender, this, alias, args);
                        }
                    };
                    map.register("nexoriashops", cmd);
                }
                if (map.getCommand("sell") == null) {
                    ShopsCommand handler = new ShopsCommand(this, true);
                    BukkitCommand cmd = new BukkitCommand("sell", "Sell items", "/sell [hand|all]", List.of()) {
                        @Override
                        public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                            return handler.onCommand(sender, this, label, args);
                        }
                        @Override
                        public List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                            return handler.onTabComplete(sender, this, alias, args);
                        }
                    };
                    map.register("nexoriashops", cmd);
                }
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static NexoriaShopsPlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaShops is not active");
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public ShopsAPI getApi() {
        return api;
    }
}
