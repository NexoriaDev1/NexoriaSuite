package dev.nexoria.chat;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Core Chat API handling format, anti-spam, word filters and mentions.
 * Thread-safe for Paper and Folia architectures.
 */
public class ChatAPI {

    public record ServiceHealth(String service, boolean active, boolean chatMuted, int filteredWordsCount, Instant timestamp) {}

    private final NexoriaChatPlugin plugin;
    private final Map<UUID, Long> lastMessageTimes = new ConcurrentHashMap<>();
    private final Set<String> blockedWords = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private volatile boolean active;
    private volatile boolean chatMuted;

    private String chatFormat;
    private double cooldownSeconds;
    private boolean filterEnabled;
    private String filterReplacement;
    private boolean mentionsEnabled;
    private String mentionColor;
    private String mentionSound;

    public ChatAPI(NexoriaChatPlugin plugin) {
        this.plugin = plugin;
        this.active = true;
        this.chatMuted = false;
        reload();
    }

    public void reload() {
        FileConfiguration config = plugin.getConfig();
        this.chatFormat = config.getString("chat.format", "&7{player}&8: &f{message}");
        this.cooldownSeconds = config.getDouble("chat.cooldown-seconds", 1.5);
        this.filterEnabled = config.getBoolean("filter.enabled", true);
        this.filterReplacement = config.getString("filter.replacement", "***");
        this.mentionsEnabled = config.getBoolean("mentions.enabled", true);
        this.mentionColor = config.getString("mentions.highlight-color", "&b@");
        this.mentionSound = config.getString("mentions.sound", "entity.experience_orb.pickup");

        this.blockedWords.clear();
        List<String> list = config.getStringList("filter.blocked-words");
        for (String w : list) {
            blockedWords.add(w.toLowerCase(Locale.ROOT));
        }
    }

    public boolean isActive() {
        return active;
    }

    public boolean isChatMuted() {
        return chatMuted;
    }

    public void setChatMuted(boolean muted) {
        this.chatMuted = muted;
    }

    public ServiceHealth getStatus() {
        return new ServiceHealth("NexoriaChat", active, chatMuted, blockedWords.size(), Instant.now());
    }

    public boolean checkCooldown(UUID playerUuid) {
        if (cooldownSeconds <= 0) return true;
        long now = System.currentTimeMillis();
        Long last = lastMessageTimes.get(playerUuid);
        if (last != null) {
            long diff = now - last;
            if (diff < (long)(cooldownSeconds * 1000)) {
                return false;
            }
        }
        lastMessageTimes.put(playerUuid, now);
        return true;
    }

    public double getRemainingCooldown(UUID playerUuid) {
        Long last = lastMessageTimes.get(playerUuid);
        if (last == null) return 0.0;
        long diff = System.currentTimeMillis() - last;
        long required = (long)(cooldownSeconds * 1000);
        if (diff >= required) return 0.0;
        return ((required - diff) / 100.0) / 10.0;
    }

    public String filterMessage(String input) {
        if (!filterEnabled || blockedWords.isEmpty()) return input;
        String result = input;
        for (String word : blockedWords) {
            Pattern pattern = Pattern.compile("(?i)\\b" + Pattern.quote(word) + "\\b");
            result = pattern.matcher(result).replaceAll(filterReplacement);
        }
        return result;
    }

    public boolean containsProhibitedWord(String input) {
        if (!filterEnabled || blockedWords.isEmpty()) return false;
        String lower = input.toLowerCase(Locale.ROOT);
        for (String word : blockedWords) {
            Pattern pattern = Pattern.compile("(?i)\\b" + Pattern.quote(word) + "\\b");
            if (pattern.matcher(lower).find()) {
                return true;
            }
        }
        return false;
    }

    public String formatMessage(Player sender, String message) {
        String worldName = sender.getWorld().getName();
        String playerName = sender.getName();
        String displayName = sender.getDisplayName();

        String formatted = chatFormat
                .replace("{player}", playerName)
                .replace("{displayname}", displayName)
                .replace("{world}", worldName);

        formatted = ChatColor.translateAlternateColorCodes('&', formatted);

        if (sender.hasPermission("nexoria.chat.color")) {
            message = ChatColor.translateAlternateColorCodes('&', message);
        }

        return formatted.replace("{message}", message);
    }

    public boolean processMentions(Player sender, String message, Collection<? extends Player> recipients) {
        if (!mentionsEnabled) return false;
        boolean anyMentioned = false;

        for (Player target : recipients) {
            if (target.equals(sender)) continue;
            String targetName = target.getName();
            Pattern p = Pattern.compile("(?i)@" + Pattern.quote(targetName));
            if (p.matcher(message).find()) {
                anyMentioned = true;
                try {
                    target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                } catch (Exception ignored) {}
            }
        }
        return anyMentioned;
    }

    public Set<String> getBlockedWords() {
        return Collections.unmodifiableSet(blockedWords);
    }

    public boolean addBlockedWord(String word) {
        boolean added = blockedWords.add(word.toLowerCase(Locale.ROOT));
        if (added) {
            plugin.getConfig().set("filter.blocked-words", new ArrayList<>(blockedWords));
            plugin.saveConfig();
        }
        return added;
    }

    public boolean removeBlockedWord(String word) {
        boolean removed = blockedWords.remove(word.toLowerCase(Locale.ROOT));
        if (removed) {
            plugin.getConfig().set("filter.blocked-words", new ArrayList<>(blockedWords));
            plugin.saveConfig();
        }
        return removed;
    }

    public void clearChat(String clearedBy) {
        String blankLines = "\n".repeat(100);
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.sendMessage(blankLines);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("messages.chat-cleared", "&eChat was cleared by &f{player}&e.")
                            .replace("{player}", clearedBy)));
        }
    }

    public void invalidateSession(UUID uuid) {
        lastMessageTimes.remove(uuid);
    }

    protected void shutdown() {
        this.active = false;
        lastMessageTimes.clear();
        blockedWords.clear();
    }
}
