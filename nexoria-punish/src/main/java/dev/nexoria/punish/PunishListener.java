package dev.nexoria.punish;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/**
 * Event listener blocking banned players on login and muted players in chat.
 */
public final class PunishListener implements Listener {

    private final NexoriaPunishPlugin plugin;

    public PunishListener(NexoriaPunishPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        PunishAPI api = plugin.getApi();
        if (api == null || !api.isActive()) return;

        if (api.isBanned(event.getUniqueId())) {
            PunishAPI.Punishment p = api.getBan(event.getUniqueId());
            String kickMsg = plugin.getConfig().getString("messages.banned-kick", "&cYou are permanently banned from this server.\n&7Reason: &f{reason}\n&7Staff: &e{staff}")
                    .replace("{reason}", p.reason())
                    .replace("{staff}", p.staff());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ChatColor.translateAlternateColorCodes('&', kickMsg));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        PunishAPI api = plugin.getApi();
        if (api == null || !api.isActive()) return;

        if (api.isMuted(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            PunishAPI.Punishment p = api.getMute(event.getPlayer().getUniqueId());
            String msg = plugin.getConfig().getString("messages.muted", "&cYou are currently muted.\n&7Reason: &f{reason}")
                    .replace("{reason}", p.reason());
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }
}
