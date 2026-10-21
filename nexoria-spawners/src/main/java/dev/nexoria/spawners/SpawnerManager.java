package dev.nexoria.spawners;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Locale;

public class SpawnerManager {

    private final NexoriaSpawnersPlugin plugin;
    private final NamespacedKey spawnerKey;

    public SpawnerManager(NexoriaSpawnersPlugin plugin) {
        this.plugin = plugin;
        this.spawnerKey = new NamespacedKey(plugin, "spawner_mob_type");
    }

    public ItemStack createSpawnerItem(EntityType type, int amount) {
        ItemStack item = new ItemStack(Material.SPAWNER, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String entityName = formatName(type.name());
            meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("&d&l" + entityName + " Spawner"));
            meta.lore(List.of(
                    LegacyComponentSerializer.legacyAmpersand().deserialize("&7Type: &f" + entityName),
                    LegacyComponentSerializer.legacyAmpersand().deserialize("&8Silk Touch Mined")
            ));

            meta.getPersistentDataContainer().set(spawnerKey, PersistentDataType.STRING, type.name());

            if (meta instanceof BlockStateMeta bsm && bsm.getBlockState() instanceof CreatureSpawner cs) {
                cs.setSpawnedType(type);
                bsm.setBlockState(cs);
            }

            item.setItemMeta(meta);
        }
        return item;
    }

    public EntityType getSpawnerType(ItemStack item) {
        if (item == null || item.getType() != Material.SPAWNER) return EntityType.PIG;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return EntityType.PIG;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(spawnerKey, PersistentDataType.STRING)) {
            String typeStr = pdc.get(spawnerKey, PersistentDataType.STRING);
            try {
                return EntityType.valueOf(typeStr);
            } catch (Exception ignored) {}
        }

        if (meta instanceof BlockStateMeta bsm && bsm.getBlockState() instanceof CreatureSpawner cs) {
            EntityType spawned = cs.getSpawnedType();
            if (spawned != null) return spawned;
        }

        return EntityType.PIG;
    }

    public void setBlockSpawnerType(Block block, EntityType type) {
        if (block.getState() instanceof CreatureSpawner cs) {
            cs.setSpawnedType(type);
            cs.update(true, true);
        }
    }

    public EntityType getBlockSpawnerType(Block block) {
        if (block.getState() instanceof CreatureSpawner cs) {
            EntityType type = cs.getSpawnedType();
            return type != null ? type : EntityType.PIG;
        }
        return EntityType.PIG;
    }

    public String formatName(String raw) {
        if (raw == null) return "Pig";
        String[] parts = raw.toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
