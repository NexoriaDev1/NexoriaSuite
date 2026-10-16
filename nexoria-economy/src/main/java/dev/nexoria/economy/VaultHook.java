package dev.nexoria.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

/**
 * Isolated Vault registration bridge.
 */
public final class VaultHook {

    private static boolean registered = false;

    public static synchronized void register(NexoriaEconomyPlugin plugin) {
        if (registered) return;
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return;

        try {
            VaultEconomyProvider provider = new VaultEconomyProvider(plugin);
            Bukkit.getServicesManager().register(Economy.class, provider, plugin, ServicePriority.Highest);
            registered = true;
            plugin.getLogger().info("Successfully registered NexoriaEconomy as Vault Economy provider.");
        } catch (Throwable t) {
            plugin.getLogger().warning("Could not register Vault provider: " + t.getMessage());
        }
    }

    public static synchronized void unregister(NexoriaEconomyPlugin plugin) {
        if (!registered) return;
        try {
            Bukkit.getServicesManager().unregister(Economy.class, plugin);
            registered = false;
        } catch (Throwable ignored) {}
    }

    public static boolean isRegistered() {
        return registered;
    }
}
