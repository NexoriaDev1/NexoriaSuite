package dev.nexoria.customitems;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public final class CustomItem {

    private final String id;
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final int customModelData;
    private final Map<Enchantment, Integer> enchantments;
    private final Set<ItemFlag> flags;
    private final boolean unbreakable;
    private final String ability;
    private final int cooldownSeconds;

    public CustomItem(String id, Material material, String name, List<String> lore, int customModelData,
                      Map<Enchantment, Integer> enchantments, Set<ItemFlag> flags, boolean unbreakable,
                      String ability, int cooldownSeconds) {
        this.id = id.toLowerCase();
        this.material = material != null ? material : Material.STICK;
        this.name = name;
        this.lore = lore != null ? new ArrayList<>(lore) : new ArrayList<>();
        this.customModelData = customModelData;
        this.enchantments = enchantments != null ? new HashMap<>(enchantments) : new HashMap<>();
        this.flags = flags != null ? new HashSet<>(flags) : new HashSet<>();
        this.unbreakable = unbreakable;
        this.ability = ability != null ? ability.toUpperCase() : "NONE";
        this.cooldownSeconds = Math.max(0, cooldownSeconds);
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return Collections.unmodifiableList(lore);
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public String getAbility() {
        return ability;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public ItemStack createItemStack(NexoriaCustomItemsPlugin plugin, int amount) {
        ItemStack item = new ItemStack(material, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (name != null && !name.isEmpty()) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        }

        if (!lore.isEmpty()) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(coloredLore);
        }

        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

        for (ItemFlag flag : flags) {
            meta.addItemFlags(flag);
        }

        meta.setUnbreakable(unbreakable);

        NamespacedKey key = new NamespacedKey(plugin, "custom_item_id");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);

        item.setItemMeta(meta);
        return item;
    }
}
