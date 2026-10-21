package dev.nexoria.shops;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Public developer API for NexoriaShops.
 */
public class ShopsAPI {

    private final NexoriaShopsPlugin plugin;

    public ShopsAPI(NexoriaShopsPlugin plugin) {
        this.plugin = plugin;
    }

    public void openShop(Player player) {
        plugin.getShopManager().openMainMenu(player);
    }

    public void openCategory(Player player, String categoryId) {
        plugin.getShopManager().openCategoryMenu(player, categoryId);
    }

    public ShopItem getItem(Material material) {
        return plugin.getShopManager().getItem(material);
    }

    public Map<String, ShopCategory> getCategories() {
        return plugin.getShopManager().getCategories();
    }

    public ShopManager getManager() {
        return plugin.getShopManager();
    }
}
