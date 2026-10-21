package dev.nexoria.cooldowns;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CooldownsListener implements Listener {

    private final NexoriaCooldownsPlugin plugin;

    public CooldownsListener(NexoriaCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("nexoria.cooldowns.bypass")) {
            return;
        }

        String raw = event.getMessage().substring(1).trim();
        String cmd = raw.split(" ")[0].toLowerCase();

        if (player.hasPermission("nexoria.cooldowns.bypass." + cmd)) {
            return;
        }

        long cdSec = plugin.getConfig().getLong("commands." + cmd, 0L);
        if (cdSec <= 0) {
            return;
        }

        CooldownManager mgr = plugin.getCooldownManager();
        if (mgr.isOnCooldown(player.getUniqueId(), "cmd_" + cmd)) {
            event.setCancelled(true);
            long rem = mgr.getRemainingMillis(player.getUniqueId(), "cmd_" + cmd);
            String formatted = mgr.formatTime(rem);

            String prefix = plugin.getConfig().getString("messages.prefix", "&8[&3Cooldowns&8] ");
            String msg = plugin.getConfig().getString("messages.on-cooldown", "&cYou must wait &e{time} &cbefore using &e/{command} &cagain!")
                    .replace("{time}", formatted)
                    .replace("{command}", cmd);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));

            if (plugin.getConfig().getBoolean("settings.notify-actionbar", true)) {
                String ab = plugin.getConfig().getString("messages.on-cooldown-actionbar", "&c/{command} on cooldown: &e{time}")
                        .replace("{time}", formatted)
                        .replace("{command}", cmd);
                player.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(ab));
            }
            return;
        }

        // Apply cooldown for future executions
        mgr.setCooldown(player.getUniqueId(), "cmd_" + cmd, cdSec);
    }
}
