package dev.nexoria.shops;

import org.bukkit.Material;

public record ShopItem(
        Material material,
        String category,
        int slot,
        double buyPrice,
        double sellPrice
) {}
