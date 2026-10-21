package dev.nexoria.shops;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.*;

public class ShopManager {

    private final NexoriaShopsPlugin plugin;
    private final Map<String, ShopCategory> categories = new LinkedHashMap<>();
    private final Map<Material, ShopItem> itemsByMaterial = new HashMap<>();
    private final Map<String, List<ShopItem>> itemsByCategory = new HashMap<>();
    private Economy economy;

    public static final String MAIN_TITLE_RAW = "&8Server Shop";
    public static final String CATEGORY_PREFIX_RAW = "&8Shop > ";

    public ShopManager(NexoriaShopsPlugin plugin) {
        this.plugin = plugin;
        setupEconomy();
        loadConfigData();
    }

    private void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.economy = rsp.getProvider();
            }
        }
    }

    public void reload() {
        categories.clear();
        itemsByMaterial.clear();
        itemsByCategory.clear();
        setupEconomy();
        loadConfigData();
    }

    private void loadConfigData() {
        // Categories
        ConfigurationSection catSec = plugin.getConfig().getConfigurationSection("categories");
        if (catSec != null) {
            for (String key : catSec.getKeys(false)) {
                ConfigurationSection sub = catSec.getConfigurationSection(key);
                if (sub == null) continue;
                String name = sub.getString("name", key);
                Material icon = Material.matchMaterial(sub.getString("icon", "CHEST"));
                if (icon == null) icon = Material.CHEST;
                int slot = sub.getInt("slot", 0);
                List<String> lore = sub.getStringList("lore");
                categories.put(key, new ShopCategory(key, name, icon, slot, lore));
            }
        }

        // Items
        ConfigurationSection itemSec = plugin.getConfig().getConfigurationSection("items");
        if (itemSec != null) {
            for (String key : itemSec.getKeys(false)) {
                ConfigurationSection sub = itemSec.getConfigurationSection(key);
                if (sub == null) continue;
                Material mat = Material.matchMaterial(key);
                if (mat == null) continue;

                String category = sub.getString("category", "misc");
                int slot = sub.getInt("slot", 0);
                double buy = sub.getDouble("buy", -1.0);
                double sell = sub.getDouble("sell", -1.0);

                ShopItem item = new ShopItem(mat, category, slot, buy, sell);
                itemsByMaterial.put(mat, item);
                itemsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(item);
            }
        }
    }

    public void openMainMenu(Player player) {
        int size = Math.min(54, Math.max(9, plugin.getConfig().getInt("settings.main-menu-size", 27)));
        String titleRaw = plugin.getConfig().getString("settings.main-menu-title", MAIN_TITLE_RAW);
        var titleComp = LegacyComponentSerializer.legacyAmpersand().deserialize(titleRaw);

        Inventory inv = Bukkit.createInventory(player, size, titleComp);

        // Fill background with grey glass panes
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < size; i++) {
            inv.setItem(i, filler);
        }

        for (ShopCategory cat : categories.values()) {
            if (cat.slot() < size) {
                List<String> formattedLore = new ArrayList<>();
                for (String l : cat.lore()) {
                    formattedLore.add(ChatColor.translateAlternateColorCodes('&', l));
                }
                inv.setItem(cat.slot(), createItem(cat.icon(), cat.name(), formattedLore));
            }
        }

        player.openInventory(inv);
    }

    public void openCategoryMenu(Player player, String categoryId) {
        ShopCategory cat = categories.get(categoryId);
        if (cat == null) return;

        int size = Math.min(54, Math.max(18, plugin.getConfig().getInt("settings.category-menu-size", 36)));
        String titleRaw = CATEGORY_PREFIX_RAW + cat.name();
        var titleComp = LegacyComponentSerializer.legacyAmpersand().deserialize(titleRaw);

        Inventory inv = Bukkit.createInventory(player, size, titleComp);

        // Bottom border
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = size - 9; i < size; i++) {
            inv.setItem(i, filler);
        }

        // Back button
        inv.setItem(size - 5, createItem(Material.BARRIER, "&c&lBack to Main Menu", List.of(ChatColor.GRAY + "Click to return.")));

        List<ShopItem> items = itemsByCategory.getOrDefault(categoryId, List.of());
        for (ShopItem item : items) {
            if (item.slot() < size - 9) {
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "-------------------");
                if (item.buyPrice() > 0) {
                    lore.add(ChatColor.GREEN + "Buy (1): " + ChatColor.YELLOW + "$" + String.format(Locale.US, "%.2f", item.buyPrice()));
                    lore.add(ChatColor.GREEN + "Buy (64): " + ChatColor.YELLOW + "$" + String.format(Locale.US, "%.2f", item.buyPrice() * 64));
                } else {
                    lore.add(ChatColor.RED + "Not purchasable");
                }
                if (item.sellPrice() > 0) {
                    lore.add(ChatColor.GOLD + "Sell (1): " + ChatColor.YELLOW + "$" + String.format(Locale.US, "%.2f", item.sellPrice()));
                    lore.add(ChatColor.GOLD + "Sell All: " + ChatColor.YELLOW + "Shift-Right-Click");
                } else {
                    lore.add(ChatColor.RED + "Not sellable");
                }
                lore.add(ChatColor.GRAY + "-------------------");
                lore.add(ChatColor.YELLOW + "Left-Click " + ChatColor.GRAY + "to Buy 1");
                lore.add(ChatColor.YELLOW + "Shift-Left-Click " + ChatColor.GRAY + "to Buy 64");
                lore.add(ChatColor.YELLOW + "Right-Click " + ChatColor.GRAY + "to Sell 1");

                String itemName = "&f" + item.material().name().replace("_", " ");
                inv.setItem(item.slot(), createItem(item.material(), itemName, lore));
            }
        }

        player.openInventory(inv);
    }

    public void handleBuy(Player player, Material mat, int amount) {
        ShopItem item = itemsByMaterial.get(mat);
        if (item == null || item.buyPrice() <= 0) return;

        double totalCost = item.buyPrice() * amount;
        if (economy == null || !economy.has(player, totalCost)) {
            String msg = plugin.getConfig().getString("messages.insufficient-funds", "&cYou need &e${price} &cto purchase this.").replace("{price}", String.format(Locale.US, "%.2f", totalCost));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&2Shop&8] ") + msg));
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            String msg = plugin.getConfig().getString("messages.inventory-full", "&cYou do not have enough inventory space.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&2Shop&8] ") + msg));
            return;
        }

        economy.withdrawPlayer(player, totalCost);
        player.getInventory().addItem(new ItemStack(mat, amount));

        String format = plugin.getConfig().getString("messages.bought", "&aYou bought &e{amount}x {item} &afor &e${price}&a.")
                .replace("{amount}", String.valueOf(amount))
                .replace("{item}", mat.name().replace("_", " "))
                .replace("{price}", String.format(Locale.US, "%.2f", totalCost));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&2Shop&8] ") + format));
    }

    public void handleSell(Player player, Material mat, int requestedAmount) {
        ShopItem item = itemsByMaterial.get(mat);
        if (item == null || item.sellPrice() <= 0) {
            String msg = plugin.getConfig().getString("messages.item-not-sellable", "&cThis item cannot be sold.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&2Shop&8] ") + msg));
            return;
        }

        int count = countItem(player, mat);
        if (count <= 0) {
            String msg = plugin.getConfig().getString("messages.no-items-to-sell", "&cYou have no items eligible to sell.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&2Shop&8] ") + msg));
            return;
        }

        int toSell = Math.min(count, requestedAmount);
        removeItem(player, mat, toSell);
        double payout = item.sellPrice() * toSell;
        if (economy != null) {
            economy.depositPlayer(player, payout);
        }

        String format = plugin.getConfig().getString("messages.sold", "&aYou sold &e{amount}x {item} &afor &e${price}&a.")
                .replace("{amount}", String.valueOf(toSell))
                .replace("{item}", mat.name().replace("_", " "))
                .replace("{price}", String.format(Locale.US, "%.2f", payout));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&2Shop&8] ") + format));
    }

    public void handleSellHand(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You are not holding any item.");
            return;
        }

        ShopItem item = itemsByMaterial.get(hand.getType());
        if (item == null || item.sellPrice() <= 0) {
            String msg = plugin.getConfig().getString("messages.item-not-sellable", "&cThis item cannot be sold.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&2Shop&8] ") + msg));
            return;
        }

        int amount = hand.getAmount();
        double payout = item.sellPrice() * amount;
        player.getInventory().setItemInMainHand(null);

        if (economy != null) {
            economy.depositPlayer(player, payout);
        }

        String format = plugin.getConfig().getString("messages.sold", "&aYou sold &e{amount}x {item} &afor &e${price}&a.")
                .replace("{amount}", String.valueOf(amount))
                .replace("{item}", hand.getType().name().replace("_", " "))
                .replace("{price}", String.format(Locale.US, "%.2f", payout));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&2Shop&8] ") + format));
    }

    public void handleSellAll(Player player) {
        int totalItems = 0;
        double totalPayout = 0.0;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack == null || stack.getType() == Material.AIR) continue;

            ShopItem item = itemsByMaterial.get(stack.getType());
            if (item != null && item.sellPrice() > 0) {
                int amount = stack.getAmount();
                totalItems += amount;
                totalPayout += item.sellPrice() * amount;
                player.getInventory().setItem(i, null);
            }
        }

        if (totalItems == 0) {
            String msg = plugin.getConfig().getString("messages.no-items-to-sell", "&cYou have no items eligible to sell.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&2Shop&8] ") + msg));
            return;
        }

        if (economy != null) {
            economy.depositPlayer(player, totalPayout);
        }

        String format = plugin.getConfig().getString("messages.sold-all", "&aSold &e{amount}x items &afor a total of &e${price}&a.")
                .replace("{amount}", String.valueOf(totalItems))
                .replace("{price}", String.format(Locale.US, "%.2f", totalPayout));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&2Shop&8] ") + format));
    }

    private int countItem(Player player, Material mat) {
        int count = 0;
        for (ItemStack is : player.getInventory().getContents()) {
            if (is != null && is.getType() == mat) {
                count += is.getAmount();
            }
        }
        return count;
    }

    private void removeItem(Player player, Material mat, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack is = contents[i];
            if (is != null && is.getType() == mat) {
                if (is.getAmount() <= remaining) {
                    remaining -= is.getAmount();
                    player.getInventory().setItem(i, null);
                } else {
                    is.setAmount(is.getAmount() - remaining);
                    remaining = 0;
                }
                if (remaining <= 0) break;
            }
        }
    }

    public Map<String, ShopCategory> getCategories() {
        return Collections.unmodifiableMap(categories);
    }

    public ShopItem getItem(Material material) {
        return itemsByMaterial.get(material);
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
            }
            if (lore != null) {
                List<net.kyori.adventure.text.Component> loreComps = new ArrayList<>();
                for (String l : lore) {
                    loreComps.add(LegacyComponentSerializer.legacyAmpersand().deserialize(l));
                }
                meta.lore(loreComps);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
