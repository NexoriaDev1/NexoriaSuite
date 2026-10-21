package dev.nexoria.announcer;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class AnnouncerManager {

    private final NexoriaAnnouncerPlugin plugin;
    private final List<Announcement> announcements = new ArrayList<>();
    private BukkitTask broadcastTask;
    private int currentIndex = 0;

    public AnnouncerManager(NexoriaAnnouncerPlugin plugin) {
        this.plugin = plugin;
        loadAnnouncements();
        startTask();
    }

    public void reload() {
        if (broadcastTask != null) {
            broadcastTask.cancel();
        }
        announcements.clear();
        currentIndex = 0;
        loadAnnouncements();
        startTask();
    }

    public void loadAnnouncements() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("announcements");
        if (section == null) return;

        Sound defaultSound = null;
        if (plugin.getConfig().getBoolean("settings.sound.enabled", true)) {
            try {
                defaultSound = Sound.valueOf(plugin.getConfig().getString("settings.sound.name", "BLOCK_NOTE_BLOCK_PLING"));
            } catch (Exception ignored) {}
        }
        float defVol = (float) plugin.getConfig().getDouble("settings.sound.volume", 1.0);
        float defPitch = (float) plugin.getConfig().getDouble("settings.sound.pitch", 1.0);

        for (String key : section.getKeys(false)) {
            ConfigurationSection sub = section.getConfigurationSection(key);
            if (sub == null) continue;

            List<String> chat = sub.getStringList("chat");
            String title = sub.getString("title");
            String subtitle = sub.getString("subtitle");
            String actionbar = sub.getString("actionbar");

            Sound s = defaultSound;
            String soundStr = sub.getString("sound");
            if (soundStr != null) {
                try {
                    s = Sound.valueOf(soundStr);
                } catch (Exception ignored) {}
            }

            announcements.add(new Announcement(key, chat, title, subtitle, actionbar, s, defVol, defPitch));
        }
    }

    public void startTask() {
        if (!plugin.getConfig().getBoolean("settings.enabled", true) || announcements.isEmpty()) {
            return;
        }

        long intervalSec = Math.max(10L, plugin.getConfig().getLong("settings.interval-seconds", 120L));
        long ticks = intervalSec * 20L;

        this.broadcastTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::broadcastNext, ticks, ticks);
    }

    public synchronized void broadcastNext() {
        if (announcements.isEmpty()) return;

        Announcement ann;
        String mode = plugin.getConfig().getString("settings.mode", "SEQUENTIAL");
        if ("RANDOM".equalsIgnoreCase(mode) && announcements.size() > 1) {
            ann = announcements.get(ThreadLocalRandom.current().nextInt(announcements.size()));
        } else {
            ann = announcements.get(currentIndex % announcements.size());
            currentIndex++;
        }

        sendAnnouncement(ann);
    }

    public void sendAnnouncement(Announcement ann) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Chat
            if (ann.chatLines() != null && !ann.chatLines().isEmpty()) {
                for (String line : ann.chatLines()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
                }
            }

            // ActionBar
            if (ann.actionBar() != null && !ann.actionBar().isEmpty()) {
                player.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(ann.actionBar()));
            }

            // Title
            if (ann.title() != null || ann.subtitle() != null) {
                var t = ann.title() != null ? LegacyComponentSerializer.legacyAmpersand().deserialize(ann.title()) : net.kyori.adventure.text.Component.empty();
                var s = ann.subtitle() != null ? LegacyComponentSerializer.legacyAmpersand().deserialize(ann.subtitle()) : net.kyori.adventure.text.Component.empty();
                player.showTitle(Title.title(t, s, Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500))));
            }

            // Sound
            if (ann.sound() != null) {
                player.playSound(player.getLocation(), ann.sound(), ann.volume(), ann.pitch());
            }
        }
    }

    public void manualBroadcast(String message) {
        String format = plugin.getConfig().getString("messages.broadcast-format", "&8[&c&lALERT&8] &f{message}");
        String formatted = ChatColor.translateAlternateColorCodes('&', format.replace("{message}", message));

        Sound alertSound = null;
        try {
            alertSound = Sound.valueOf("BLOCK_BELL_USE");
        } catch (Exception ignored) {}

        Bukkit.getConsoleSender().sendMessage(formatted);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(formatted);
            if (alertSound != null) {
                player.playSound(player.getLocation(), alertSound, 1.0f, 1.0f);
            }
        }
    }

    public List<Announcement> getAnnouncements() {
        return Collections.unmodifiableList(announcements);
    }

    public void shutdown() {
        if (broadcastTask != null) {
            broadcastTask.cancel();
        }
        announcements.clear();
    }
}
