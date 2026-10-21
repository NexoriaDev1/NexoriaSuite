package dev.nexoria.customcraft;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public final class RecipeManager {

    private final NexoriaCustomCraftPlugin plugin;
    private final Set<NamespacedKey> registeredKeys = new HashSet<>();

    public RecipeManager(NexoriaCustomCraftPlugin plugin) {
        this.plugin = plugin;
        loadRecipes();
    }

    public synchronized void loadRecipes() {
        unregisterAll();

        File file = new File(plugin.getDataFolder(), "recipes.yml");
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try (InputStream in = plugin.getResource("recipes.yml")) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to copy recipes.yml", e);
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection shapedSec = config.getConfigurationSection("shaped-recipes");
        if (shapedSec != null) {
            for (String key : shapedSec.getKeys(false)) {
                loadShapedRecipe(key, shapedSec.getConfigurationSection(key));
            }
        }

        ConfigurationSection shapelessSec = config.getConfigurationSection("shapeless-recipes");
        if (shapelessSec != null) {
            for (String key : shapelessSec.getKeys(false)) {
                loadShapelessRecipe(key, shapelessSec.getConfigurationSection(key));
            }
        }

        plugin.getLogger().info("Registered " + registeredKeys.size() + " custom recipes.");
    }

    private void loadShapedRecipe(String key, ConfigurationSection sec) {
        if (sec == null) return;
        try {
            ItemStack result = parseResult(sec.getString("result"), sec.getInt("amount", 1));
            if (result == null) return;

            NamespacedKey nKey = new NamespacedKey(plugin, key.toLowerCase());
            ShapedRecipe recipe = new ShapedRecipe(nKey, result);

            List<String> shape = sec.getStringList("shape");
            if (shape.isEmpty() || shape.size() > 3) {
                plugin.getLogger().warning("Recipe " + key + " shape must have between 1 and 3 rows.");
                return;
            }
            recipe.shape(shape.toArray(new String[0]));

            ConfigurationSection ingSec = sec.getConfigurationSection("ingredients");
            if (ingSec != null) {
                for (String charKey : ingSec.getKeys(false)) {
                    if (charKey.length() != 1) continue;
                    char c = charKey.charAt(0);
                    String matName = ingSec.getString(charKey, "");
                    Material mat = Material.matchMaterial(matName.toUpperCase());
                    if (mat == null) {
                        plugin.getLogger().warning("Recipe " + key + " has unknown material: " + matName);
                        return;
                    }
                    recipe.setIngredient(c, mat);
                }
            }

            Bukkit.addRecipe(recipe);
            registeredKeys.add(nKey);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to register shaped recipe: " + key, e);
        }
    }

    private void loadShapelessRecipe(String key, ConfigurationSection sec) {
        if (sec == null) return;
        try {
            ItemStack result = parseResult(sec.getString("result"), sec.getInt("amount", 1));
            if (result == null) return;

            NamespacedKey nKey = new NamespacedKey(plugin, key.toLowerCase());
            ShapelessRecipe recipe = new ShapelessRecipe(nKey, result);

            List<String> ingredients = sec.getStringList("ingredients");
            for (String ingStr : ingredients) {
                Material mat = Material.matchMaterial(ingStr.toUpperCase());
                if (mat != null) {
                    recipe.addIngredient(mat);
                }
            }

            Bukkit.addRecipe(recipe);
            registeredKeys.add(nKey);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to register shapeless recipe: " + key, e);
        }
    }

    private ItemStack parseResult(String resultStr, int amount) {
        if (resultStr == null || resultStr.isEmpty()) return null;
        Material mat = Material.matchMaterial(resultStr.toUpperCase());
        if (mat != null) {
            return new ItemStack(mat, Math.max(1, amount));
        }
        return null;
    }

    public synchronized void unregisterAll() {
        for (NamespacedKey key : registeredKeys) {
            try {
                Bukkit.removeRecipe(key);
            } catch (Throwable ignored) {}
        }
        registeredKeys.clear();
    }

    public Set<NamespacedKey> getRegisteredKeys() {
        return Collections.unmodifiableSet(registeredKeys);
    }
}
