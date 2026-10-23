package dev.nexoria.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

/**
 * Isolated Vault registration bridge.
 */
public final class VaultHook {

    private static VaultEconomyProvider provider = null;

    public static synchronized void register(NexoriaEconomyPlugin plugin) {
        if (provider != null) return;
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return;

        try {
            provider = new VaultEconomyProvider(plugin);
            Bukkit.getServicesManager().register(Economy.class, provider, plugin, ServicePriority.Highest);
            plugin.getLogger().info("Successfully registered NexoriaEconomy as Vault Economy provider.");
        } catch (Throwable t) {
            plugin.getLogger().warning("Could not register Vault provider: " + t.getMessage());
        }
    }

    public static synchronized void unregister(NexoriaEconomyPlugin plugin) {
        if (provider == null) return;
        try {
            Bukkit.getServicesManager().unregisterAll(plugin);
            provider = null;
        } catch (Throwable ignored) {}
    }

    public static boolean isRegistered() {
        return provider != null;
    }
}
