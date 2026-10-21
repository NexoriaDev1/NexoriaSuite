package dev.nexoria.menu;

import org.bukkit.entity.Player;

/**
 * Public developer API for NexoriaMenu.
 */
public class MenuAPI {

    private final NexoriaMenuPlugin plugin;

    public MenuAPI(NexoriaMenuPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player) {
        plugin.getMenuManager().openMainMenu(player);
    }

    public MenuManager getManager() {
        return plugin.getMenuManager();
    }
}
