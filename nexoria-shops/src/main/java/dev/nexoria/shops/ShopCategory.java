package dev.nexoria.shops;

import org.bukkit.Material;
import java.util.List;

public record ShopCategory(
        String id,
        String name,
        Material icon,
        int slot,
        List<String> lore
) {}
