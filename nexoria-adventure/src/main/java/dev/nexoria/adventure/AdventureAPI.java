package dev.nexoria.adventure;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

/**
 * Public developer API for NexoriaAdventure.
 */
public class AdventureAPI {

    private final NexoriaAdventurePlugin plugin;

    public AdventureAPI(NexoriaAdventurePlugin plugin) {
        this.plugin = plugin;
    }

    public Component parse(String text) {
        return plugin.getAdventureManager().parse(text);
    }

    public String toLegacy(Component component) {
        return plugin.getAdventureManager().toLegacy(component);
    }

    public void broadcastBossBar(String text, int seconds, BossBar.Color color) {
        plugin.getAdventureManager().broadcastBossBar(text, seconds, color);
    }

    public AdventureManager getManager() {
        return plugin.getAdventureManager();
    }
}
