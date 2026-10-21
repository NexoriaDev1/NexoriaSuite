package dev.nexoria.skills;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class SkillManager {

    private final NexoriaSkillsPlugin plugin;
    private final Map<UUID, SkillProfile> profiles = new ConcurrentHashMap<>();
    private final File dataFile;

    public SkillManager(NexoriaSkillsPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "skills_data.yml");
        loadData();
    }

    public synchronized void loadData() {
        profiles.clear();
        if (!dataFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection root = config.getConfigurationSection("profiles");
        if (root == null) return;

        for (String uuidStr : root.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                SkillProfile profile = new SkillProfile(uuid);
                ConfigurationSection pSec = root.getConfigurationSection(uuidStr);
                if (pSec != null) {
                    for (SkillType s : SkillType.values()) {
                        int lvl = pSec.getInt(s.name() + ".level", 1);
                        double xp = pSec.getDouble(s.name() + ".xp", 0.0);
                        profile.setLevel(s, lvl);
                        profile.setExp(s, xp);
                    }
                }
                profiles.put(uuid, profile);
            } catch (Exception ignored) {}
        }
        plugin.getLogger().info("Loaded " + profiles.size() + " player skill profiles.");
    }

    public synchronized void saveData() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection root = config.createSection("profiles");

        for (Map.Entry<UUID, SkillProfile> entry : profiles.entrySet()) {
            String uStr = entry.getKey().toString();
            SkillProfile sp = entry.getValue();
            for (SkillType s : SkillType.values()) {
                root.set(uStr + "." + s.name() + ".level", sp.getLevel(s));
                root.set(uStr + "." + s.name() + ".xp", sp.getExp(s));
            }
        }

        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save skills data", e);
        }
    }

    public SkillProfile getProfile(UUID uuid) {
        return profiles.computeIfAbsent(uuid, SkillProfile::new);
    }

    public void awardExp(Player player, SkillType skill, double amount) {
        SkillProfile profile = getProfile(player.getUniqueId());
        boolean leveledUp = profile.addExp(skill, amount);

        int curLvl = profile.getLevel(skill);
        double curXp = profile.getExp(skill);
        double reqXp = profile.getRequiredExp(skill);

        String actionBarMsg = "&a+" + String.format("%.0f", amount) + " " + skill.getDisplayName() + " XP &8[&7Lv. " + curLvl + ": " + String.format("%.0f", curXp) + "/" + String.format("%.0f", reqXp) + "&8]";
        try {
            player.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(actionBarMsg));
        } catch (Throwable ignored) {}

        if (leveledUp) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
            Component title = LegacyComponentSerializer.legacyAmpersand().deserialize("&6&lSKILL LEVEL UP!");
            Component subtitle = LegacyComponentSerializer.legacyAmpersand().deserialize("&e" + skill.getDisplayName() + " &7is now Level &a" + curLvl + "§7!");
            try {
                player.showTitle(Title.title(title, subtitle, Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(2000), Duration.ofMillis(500))));
            } catch (Throwable ignored) {}

            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&8[&6Skills&8] &aCongratulations! Your &e" + skill.getDisplayName() + " &askill reached &fLevel " + curLvl + "&a!"));
            saveData();
        }
    }

    public Map<UUID, SkillProfile> getAllProfiles() {
        return Collections.unmodifiableMap(profiles);
    }
}
