package dev.nexoria.chat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Chat Listener ensuring cross-platform compatibility with modern Paper, Purpur, and Folia.
 */
public final class ChatListener implements Listener {

    private final NexoriaChatPlugin plugin;

    public ChatListener(NexoriaChatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ChatAPI api = plugin.getApi();

        if (api == null || !api.isActive()) {
            return;
        }

        // Global chat mute check
        if (api.isChatMuted() && !player.hasPermission("nexoria.chat.admin")) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.chat-is-muted", "&cGlobal chat is currently muted.")));
            return;
        }

        // Anti-spam cooldown check
        if (!player.hasPermission("nexoria.chat.bypass.cooldown")) {
            if (!api.checkCooldown(player.getUniqueId())) {
                event.setCancelled(true);
                double rem = api.getRemainingCooldown(player.getUniqueId());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.cooldown", "&cPlease wait &e{seconds}s &cbefore sending another message.")
                                .replace("{seconds}", String.valueOf(rem))));
                return;
            }
        }

        String rawMessage = event.getMessage();

        // Word filter
        if (!player.hasPermission("nexoria.chat.bypass.filter")) {
            rawMessage = api.filterMessage(rawMessage);
        }

        // Process @mentions with sounds
        api.processMentions(player, rawMessage, event.getRecipients());

        // Format message
        String formatted = api.formatMessage(player, rawMessage);
        event.setFormat(formatted.replace("%", "%%"));
        event.setMessage(rawMessage);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getApi() != null) {
            plugin.getApi().invalidateSession(event.getPlayer().getUniqueId());
        }
    }
}
