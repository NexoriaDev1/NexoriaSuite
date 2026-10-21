package dev.nexoria.shops;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopsListener implements Listener {

    private final NexoriaShopsPlugin plugin;

    public ShopsListener(NexoriaShopsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        boolean isMainMenu = title.contains("Server Shop");
        boolean isCategoryMenu = title.contains("Shop >");

        if (!isMainMenu && !isCategoryMenu) {
            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        // Main Menu
        if (isMainMenu) {
            for (ShopCategory cat : plugin.getShopManager().getCategories().values()) {
                if (event.getRawSlot() == cat.slot()) {
                    plugin.getShopManager().openCategoryMenu(player, cat.id());
                    return;
                }
            }
            return;
        }

        // Category Menu
        if (isCategoryMenu) {
            int size = event.getInventory().getSize();
            if (event.getRawSlot() == size - 5) {
                // Back button
                plugin.getShopManager().openMainMenu(player);
                return;
            }

            ShopItem item = plugin.getShopManager().getItem(clicked.getType());
            if (item == null) {
                return;
            }

            ClickType click = event.getClick();
            if (click == ClickType.LEFT) {
                // Buy 1
                plugin.getShopManager().handleBuy(player, item.material(), 1);
            } else if (click == ClickType.SHIFT_LEFT) {
                // Buy 64
                plugin.getShopManager().handleBuy(player, item.material(), 64);
            } else if (click == ClickType.RIGHT) {
                // Sell 1
                plugin.getShopManager().handleSell(player, item.material(), 1);
            } else if (click == ClickType.SHIFT_RIGHT) {
                // Sell all
                plugin.getShopManager().handleSell(player, item.material(), Integer.MAX_VALUE);
            }
        }
    }
}
