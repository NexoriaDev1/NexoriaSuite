package dev.nexoria.announcer;

import org.bukkit.Sound;

import java.util.List;

public record Announcement(
        String id,
        List<String> chatLines,
        String title,
        String subtitle,
        String actionBar,
        Sound sound,
        float volume,
        float pitch
) {}
