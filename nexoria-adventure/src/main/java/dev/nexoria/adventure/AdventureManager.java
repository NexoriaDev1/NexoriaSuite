package dev.nexoria.adventure;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicInteger;

public class AdventureManager {

    private final NexoriaAdventurePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public AdventureManager(NexoriaAdventurePlugin plugin) {
        this.plugin = plugin;
    }

    public Component parse(String input) {
        if (input == null || input.isEmpty()) return Component.empty();
        // If input contains legacy ampersands, convert first or parse
        if (input.contains("&")) {
            input = input.replace("&0", "<black>")
                    .replace("&1", "<dark_blue>")
                    .replace("&2", "<dark_green>")
                    .replace("&3", "<dark_aqua>")
                    .replace("&4", "<dark_red>")
                    .replace("&5", "<dark_purple>")
                    .replace("&6", "<gold>")
                    .replace("&7", "<gray>")
                    .replace("&8", "<dark_gray>")
                    .replace("&9", "<blue>")
                    .replace("&a", "<green>")
                    .replace("&b", "<aqua>")
                    .replace("&c", "<red>")
                    .replace("&d", "<light_purple>")
                    .replace("&e", "<yellow>")
                    .replace("&f", "<white>")
                    .replace("&l", "<bold>")
                    .replace("&o", "<italic>")
                    .replace("&n", "<underlined>")
                    .replace("&m", "<strikethrough>")
                    .replace("&r", "<reset>");
        }
        return miniMessage.deserialize(input);
    }

    public String toLegacy(Component component) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }

    public void broadcastBossBar(String text, int seconds, BossBar.Color color) {
        Component titleComp = parse(text);
        BossBar.Overlay overlay = BossBar.Overlay.PROGRESS;
        try {
            overlay = BossBar.Overlay.valueOf(plugin.getConfig().getString("settings.default-bossbar-overlay", "PROGRESS").toUpperCase());
        } catch (Exception ignored) {}

        BossBar bar = BossBar.bossBar(titleComp, 1.0f, color, overlay);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showBossBar(bar);
        }

        int totalTicks = Math.max(20, seconds * 20);
        AtomicInteger elapsed = new AtomicInteger(0);

        BukkitTask[] taskRef = new BukkitTask[1];
        taskRef[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            int current = elapsed.addAndGet(2);
            if (current >= totalTicks) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.hideBossBar(bar);
                }
                if (taskRef[0] != null) {
                    taskRef[0].cancel();
                }
            } else {
                float progress = Math.max(0.0f, 1.0f - ((float) current / (float) totalTicks));
                bar.progress(progress);
            }
        }, 2L, 2L);
    }
}
