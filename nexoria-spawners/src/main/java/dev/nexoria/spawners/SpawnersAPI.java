package dev.nexoria.spawners;

import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

/**
 * Public developer API for NexoriaSpawners.
 */
public class SpawnersAPI {

    private final NexoriaSpawnersPlugin plugin;

    public SpawnersAPI(NexoriaSpawnersPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createSpawner(EntityType type, int amount) {
        return plugin.getSpawnerManager().createSpawnerItem(type, amount);
    }

    public EntityType getSpawnerType(ItemStack item) {
        return plugin.getSpawnerManager().getSpawnerType(item);
    }

    public void setBlockSpawnerType(Block block, EntityType type) {
        plugin.getSpawnerManager().setBlockSpawnerType(block, type);
    }

    public EntityType getBlockSpawnerType(Block block) {
        return plugin.getSpawnerManager().getBlockSpawnerType(block);
    }

    public SpawnerManager getManager() {
        return plugin.getSpawnerManager();
    }
}
