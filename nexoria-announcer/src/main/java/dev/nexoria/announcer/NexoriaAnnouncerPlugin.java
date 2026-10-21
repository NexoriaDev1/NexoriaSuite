package dev.nexoria.announcer;

import io.papermc.paper.command.brigadier.BasicCommand;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class NexoriaAnnouncerPlugin extends JavaPlugin {

    private static NexoriaAnnouncerPlugin instance;
    private AnnouncerManager announcerManager;
    private AnnouncerAPI api;

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
                    registerCommand(registrar, registrarClass, "announcer", "Manage scheduled announcements", List.of(), new AnnouncerCommand(this, false));
                    registerCommand(registrar, registrarClass, "broadcast", "Broadcast custom announcement", List.of("bc"), new AnnouncerCommand(this, true));
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

        this.announcerManager = new AnnouncerManager(this);
        this.api = new AnnouncerAPI(this);

        fallbackRegisterCommands();

        getServer().getPluginManager().registerEvents(new AnnouncerListener(this), this);

        getLogger().log(Level.INFO, "NexoriaAnnouncer v{0} successfully enabled.", getPluginMeta().getVersion());
    }

    private void fallbackRegisterCommands() {
        try {
            Method getCommandMap = getServer().getClass().getMethod("getCommandMap");
            CommandMap map = (CommandMap) getCommandMap.invoke(getServer());
            if (map != null) {
                if (map.getCommand("announcer") == null) {
                    AnnouncerCommand handler = new AnnouncerCommand(this, false);
                    BukkitCommand cmd = new BukkitCommand("announcer", "Manage announcements", "/announcer", List.of()) {
                        @Override
                        public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                            return handler.onCommand(sender, this, label, args);
                        }
                        @Override
                        public List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                            return handler.onTabComplete(sender, this, alias, args);
                        }
                    };
                    map.register("nexoriaannouncer", cmd);
                }
                if (map.getCommand("broadcast") == null) {
                    AnnouncerCommand handler = new AnnouncerCommand(this, true);
                    BukkitCommand cmd = new BukkitCommand("broadcast", "Broadcast alert", "/broadcast <message>", List.of("bc")) {
                        @Override
                        public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                            return handler.onCommand(sender, this, label, args);
                        }
                    };
                    map.register("nexoriaannouncer", cmd);
                }
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void onDisable() {
        if (announcerManager != null) {
            announcerManager.shutdown();
        }
        instance = null;
    }

    public static NexoriaAnnouncerPlugin getInstance() {
        return Objects.requireNonNull(instance, "NexoriaAnnouncer is not active");
    }

    public AnnouncerManager getAnnouncerManager() {
        return announcerManager;
    }

    public AnnouncerAPI getApi() {
        return api;
    }
}
