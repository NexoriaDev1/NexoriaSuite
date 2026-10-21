package dev.nexoria.menu;

import org.bukkit.Material;
import java.util.List;

public record MenuItem(
        Material material,
        int slot,
        String name,
        List<String> lore,
        String action
) {}
