package dev.nexoria.menu;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MenuManager {

    private final NexoriaMenuPlugin plugin;
    private final Map<Integer, MenuItem> mainMenuItems = new HashMap<>();
    private String mainMenuTitle;
    private int mainMenuSize;

    public MenuManager(NexoriaMenuPlugin plugin) {
        this.plugin = plugin;
        loadMenus();
    }

    public void reload() {
        mainMenuItems.clear();
        loadMenus();
    }

    public void loadMenus() {
        ConfigurationSection menuSec = plugin.getConfig().getConfigurationSection("main-menu");
        if (menuSec == null) return;

        this.mainMenuTitle = menuSec.getString("title", "&8Server Navigation");
        this.mainMenuSize = Math.min(54, Math.max(9, menuSec.getInt("size", 27)));

        ConfigurationSection items = menuSec.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection sub = items.getConfigurationSection(key);
                if (sub == null) continue;

                Material mat = Material.matchMaterial(sub.getString("material", "STONE"));
                if (mat == null) mat = Material.STONE;

                int slot = sub.getInt("slot", 0);
                String name = sub.getString("name", key);
                List<String> lore = sub.getStringList("lore");
                String action = sub.getString("action", "");

                mainMenuItems.put(slot, new MenuItem(mat, slot, name, lore, action));
            }
        }
    }

    public void openMainMenu(Player player) {
        var titleComp = LegacyComponentSerializer.legacyAmpersand().deserialize(mainMenuTitle);
        Inventory inv = Bukkit.createInventory(player, mainMenuSize, titleComp);

        // Fill background
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < mainMenuSize; i++) {
            inv.setItem(i, filler);
        }

        // Set items
        for (MenuItem item : mainMenuItems.values()) {
            if (item.slot() < mainMenuSize) {
                List<String> formattedLore = new ArrayList<>();
                for (String l : item.lore()) {
                    formattedLore.add(ChatColor.translateAlternateColorCodes('&', l));
                }
                inv.setItem(item.slot(), createItem(item.material(), item.name(), formattedLore));
            }
        }

        player.openInventory(inv);
    }

    public void handleClick(Player player, int slot) {
        MenuItem item = mainMenuItems.get(slot);
        if (item == null || item.action() == null || item.action().isEmpty()) {
            return;
        }

        playClickSound(player);

        String action = item.action();
        if (action.startsWith("COMMAND:")) {
            String cmd = action.substring("COMMAND:".length()).trim();
            player.closeInventory();
            player.performCommand(cmd);
        } else if (action.startsWith("CONSOLE:")) {
            String cmd = action.substring("CONSOLE:".length()).trim().replace("%player%", player.getName());
            player.closeInventory();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        } else if (action.startsWith("MESSAGE:")) {
            String msg = action.substring("MESSAGE:".length()).trim();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        } else if (action.equalsIgnoreCase("CLOSE")) {
            player.closeInventory();
        }
    }

    private void playClickSound(Player player) {
        if (!plugin.getConfig().getBoolean("settings.sound.enabled", true)) return;
        try {
            Sound s = Sound.valueOf(plugin.getConfig().getString("settings.sound.name", "UI_BUTTON_CLICK"));
            float v = (float) plugin.getConfig().getDouble("settings.sound.volume", 1.0);
            float p = (float) plugin.getConfig().getDouble("settings.sound.pitch", 1.2);
            player.playSound(player.getLocation(), s, v, p);
        } catch (Exception ignored) {}
    }

    public String getMainMenuTitle() {
        return mainMenuTitle;
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
            }
            if (lore != null) {
                List<net.kyori.adventure.text.Component> comps = new ArrayList<>();
                for (String l : lore) {
                    comps.add(LegacyComponentSerializer.legacyAmpersand().deserialize(l));
                }
                meta.lore(comps);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
