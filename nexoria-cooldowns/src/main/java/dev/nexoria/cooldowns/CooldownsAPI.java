package dev.nexoria.cooldowns;

import java.util.UUID;

/**
 * Public developer API for NexoriaCooldowns.
 */
public class CooldownsAPI {

    private final NexoriaCooldownsPlugin plugin;

    public CooldownsAPI(NexoriaCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isOnCooldown(UUID uuid, String key) {
        return plugin.getCooldownManager().isOnCooldown(uuid, key);
    }

    public long getRemainingSeconds(UUID uuid, String key) {
        return plugin.getCooldownManager().getRemainingMillis(uuid, key) / 1000L;
    }

    public void setCooldown(UUID uuid, String key, long seconds) {
        plugin.getCooldownManager().setCooldown(uuid, key, seconds);
    }

    public void resetCooldown(UUID uuid, String key) {
        plugin.getCooldownManager().resetCooldown(uuid, key);
    }

    public CooldownManager getManager() {
        return plugin.getCooldownManager();
    }
}
