package dev.nexoria.worldguard;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public final class WorldGuardHook {

    private final NexoriaWorldGuardBridgePlugin plugin;
    private boolean available = false;

    private Object worldGuardInstance;
    private Object regionContainer;
    private Class<?> bukkitAdapterClass;
    private Method adaptLocationMethod;
    private Method adaptPlayerMethod;
    private Object buildFlag;
    private Object pvpFlag;

    public WorldGuardHook(NexoriaWorldGuardBridgePlugin plugin) {
        this.plugin = plugin;
        init();
    }

    public void init() {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            available = false;
            plugin.getLogger().info("WorldGuard not detected. Operating in standalone fallback mode.");
            return;
        }

        try {
            Class<?> wgClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Method getInstanceMethod = wgClass.getMethod("getInstance");
            this.worldGuardInstance = getInstanceMethod.invoke(null);

            Method getPlatformMethod = wgClass.getMethod("getPlatform");
            Object platform = getPlatformMethod.invoke(worldGuardInstance);

            Method getRegionContainerMethod = platform.getClass().getMethod("getRegionContainer");
            this.regionContainer = getRegionContainerMethod.invoke(platform);

            this.bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            this.adaptLocationMethod = bukkitAdapterClass.getMethod("adapt", Location.class);
            this.adaptPlayerMethod = bukkitAdapterClass.getMethod("adapt", Player.class);

            Class<?> flagsClass = Class.forName("com.sk89q.worldguard.protection.flags.Flags");
            this.buildFlag = flagsClass.getField("BUILD").get(null);
            this.pvpFlag = flagsClass.getField("PVP").get(null);

            available = true;
            plugin.getLogger().info("Successfully hooked into WorldGuard 7+ platform.");
        } catch (Throwable t) {
            available = false;
            plugin.getLogger().log(Level.WARNING, "Failed to initialize WorldGuard reflection bridge: " + t.getMessage());
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean canBuild(Player player, Location location) {
        if (!available) return true;
        try {
            Object weLoc = adaptLocationMethod.invoke(null, location);
            Object wePlayer = adaptPlayerMethod.invoke(null, player);

            Method createQueryMethod = regionContainer.getClass().getMethod("createQuery");
            Object query = createQueryMethod.invoke(regionContainer);

            Class<?> flagClass = Class.forName("com.sk89q.worldguard.protection.flags.StateFlag");
            Object flagArray = java.lang.reflect.Array.newInstance(flagClass, 1);
            java.lang.reflect.Array.set(flagArray, 0, buildFlag);

            Class<?> locClass = Class.forName("com.sk89q.worldedit.util.Location");
            Class<?> assocClass = Class.forName("com.sk89q.worldguard.protection.association.RegionAssociable");

            Method testStateMethod = query.getClass().getMethod("testState", locClass, assocClass, flagArray.getClass());
            Object result = testStateMethod.invoke(query, weLoc, wePlayer, flagArray);
            return result instanceof Boolean b ? b : true;
        } catch (Throwable t) {
            return true;
        }
    }

    public boolean canPvP(Player player, Location location) {
        if (!available) return true;
        try {
            Object weLoc = adaptLocationMethod.invoke(null, location);
            Object wePlayer = adaptPlayerMethod.invoke(null, player);

            Method createQueryMethod = regionContainer.getClass().getMethod("createQuery");
            Object query = createQueryMethod.invoke(regionContainer);

            Class<?> flagClass = Class.forName("com.sk89q.worldguard.protection.flags.StateFlag");
            Object flagArray = java.lang.reflect.Array.newInstance(flagClass, 1);
            java.lang.reflect.Array.set(flagArray, 0, pvpFlag);

            Class<?> locClass = Class.forName("com.sk89q.worldedit.util.Location");
            Class<?> assocClass = Class.forName("com.sk89q.worldguard.protection.association.RegionAssociable");

            Method testStateMethod = query.getClass().getMethod("testState", locClass, assocClass, flagArray.getClass());
            Object result = testStateMethod.invoke(query, weLoc, wePlayer, flagArray);
            return result instanceof Boolean b ? b : true;
        } catch (Throwable t) {
            return true;
        }
    }

    public List<String> getRegions(Location location) {
        if (!available) return Collections.emptyList();
        try {
            Object weLoc = adaptLocationMethod.invoke(null, location);
            Method createQueryMethod = regionContainer.getClass().getMethod("createQuery");
            Object query = createQueryMethod.invoke(regionContainer);

            Class<?> locClass = Class.forName("com.sk89q.worldedit.util.Location");
            Method getApplicableRegions = query.getClass().getMethod("getApplicableRegions", locClass);
            Object set = getApplicableRegions.invoke(query, weLoc);

            Method getRegionsMethod = set.getClass().getMethod("getRegions");
            Iterable<?> regions = (Iterable<?>) getRegionsMethod.invoke(set);

            List<String> names = new ArrayList<>();
            for (Object r : regions) {
                Method getIdMethod = r.getClass().getMethod("getId");
                names.add((String) getIdMethod.invoke(r));
            }
            return names;
        } catch (Throwable t) {
            return Collections.emptyList();
        }
    }
}
