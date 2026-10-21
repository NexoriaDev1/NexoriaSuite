package dev.nexoria.quests;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class QuestManager {

    private final NexoriaQuestsPlugin plugin;
    private final Map<String, Quest> quests = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, QuestProgress>> playerProgress = new ConcurrentHashMap<>();
    private final File progressFile;
    private Economy economy;

    public QuestManager(NexoriaQuestsPlugin plugin) {
        this.plugin = plugin;
        this.progressFile = new File(plugin.getDataFolder(), "player_quests.yml");
        setupEconomy();
        loadQuests();
        loadProgress();
    }

    private void setupEconomy() {
        try {
            if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
                if (rsp != null) {
                    this.economy = rsp.getProvider();
                }
            }
        } catch (Throwable ignored) {}
    }

    public synchronized void loadQuests() {
        quests.clear();
        File file = new File(plugin.getDataFolder(), "quests.yml");
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try (InputStream in = plugin.getResource("quests.yml")) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to copy quests.yml", e);
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = config.getConfigurationSection("quests");
        if (root == null) {
            registerDefaults();
            return;
        }

        for (String key : root.getKeys(false)) {
            try {
                ConfigurationSection sec = root.getConfigurationSection(key);
                if (sec == null) continue;

                String name = sec.getString("name", key);
                String desc = sec.getString("description", "");
                String typeStr = sec.getString("type", "KILL_MOB").toUpperCase();
                Quest.QuestType type = Quest.QuestType.valueOf(typeStr);
                String target = sec.getString("target", "ZOMBIE");
                int required = sec.getInt("required", 10);
                double money = sec.getDouble("reward.money", 50.0);
                int xp = sec.getInt("reward.xp", 100);

                Quest q = new Quest(key, name, desc, type, target, required, money, xp);
                quests.put(q.getId(), q);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load quest: " + key, e);
            }
        }

        if (quests.isEmpty()) {
            registerDefaults();
        }

        plugin.getLogger().info("Loaded " + quests.size() + " active quests.");
    }

    private void registerDefaults() {
        Quest q1 = new Quest("zombie_hunter", "Zombie Hunter", "Defeat 15 Zombies in combat", Quest.QuestType.KILL_MOB, "ZOMBIE", 15, 100.0, 150);
        Quest q2 = new Quest("skeleton_slayer", "Skeleton Slayer", "Defeat 10 Skeletons", Quest.QuestType.KILL_MOB, "SKELETON", 10, 75.0, 100);
        Quest q3 = new Quest("iron_miner", "Iron Miner", "Extract 32 raw iron ores", Quest.QuestType.MINE_BLOCK, "IRON_ORE", 32, 120.0, 200);
        quests.put(q1.getId(), q1);
        quests.put(q2.getId(), q2);
        quests.put(q3.getId(), q3);
    }

    public synchronized void loadProgress() {
        playerProgress.clear();
        if (!progressFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(progressFile);
        ConfigurationSection root = config.getConfigurationSection("players");
        if (root == null) return;

        for (String uuidStr : root.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection pSec = root.getConfigurationSection(uuidStr);
                if (pSec == null) continue;

                Map<String, QuestProgress> map = new ConcurrentHashMap<>();
                for (String qId : pSec.getKeys(false)) {
                    int cur = pSec.getInt(qId + ".progress", 0);
                    boolean comp = pSec.getBoolean(qId + ".completed", false);
                    long compAt = pSec.getLong(qId + ".completed-at", 0L);
                    map.put(qId.toLowerCase(), new QuestProgress(uuid, qId.toLowerCase(), cur, comp, compAt));
                }
                playerProgress.put(uuid, map);
            } catch (Exception ignored) {}
        }
    }

    public synchronized void saveProgress() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection root = config.createSection("players");

        for (Map.Entry<UUID, Map<String, QuestProgress>> entry : playerProgress.entrySet()) {
            String uStr = entry.getKey().toString();
            for (Map.Entry<String, QuestProgress> qEntry : entry.getValue().entrySet()) {
                QuestProgress qp = qEntry.getValue();
                root.set(uStr + "." + qEntry.getKey() + ".progress", qp.getCurrentProgress());
                root.set(uStr + "." + qEntry.getKey() + ".completed", qp.isCompleted());
                root.set(uStr + "." + qEntry.getKey() + ".completed-at", qp.getCompletedAt());
            }
        }

        try {
            config.save(progressFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save quest progress", e);
        }
    }

    public QuestProgress getProgress(UUID playerUuid, String questId) {
        return playerProgress.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(questId.toLowerCase(), k -> new QuestProgress(playerUuid, questId.toLowerCase(), 0, false, 0L));
    }

    public void handleProgress(Player player, Quest.QuestType type, String target, int amount) {
        for (Quest quest : quests.values()) {
            if (quest.getType() != type) continue;
            if (!quest.getTarget().equalsIgnoreCase(target)) continue;

            QuestProgress progress = getProgress(player.getUniqueId(), quest.getId());
            if (progress.isCompleted()) continue;

            int newProg = progress.getCurrentProgress() + amount;
            progress.setCurrentProgress(newProg);

            if (newProg >= quest.getRequiredAmount()) {
                progress.setCurrentProgress(quest.getRequiredAmount());
                progress.setCompleted(true);
                progress.setCompletedAt(System.currentTimeMillis());
                rewardPlayer(player, quest);
            }
        }
    }

    private void rewardPlayer(Player player, Quest quest) {
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        player.sendMessage("§8==================================================");
        player.sendMessage("§a§lQUEST COMPLETED: §f" + quest.getName());
        player.sendMessage("§7" + quest.getDescription());
        if (quest.getRewardXp() > 0) {
            player.giveExp(quest.getRewardXp());
            player.sendMessage("§7• Reward: §a+" + quest.getRewardXp() + " XP");
        }
        if (quest.getRewardMoney() > 0) {
            if (economy != null) {
                economy.depositPlayer(player, quest.getRewardMoney());
                player.sendMessage("§7• Reward: §e+$" + String.format("%.2f", quest.getRewardMoney()));
            } else {
                player.sendMessage("§7• Reward: §e$" + String.format("%.2f", quest.getRewardMoney()) + " §8(Economy not hooked)");
            }
        }
        player.sendMessage("§8==================================================");
        saveProgress();
    }

    public Map<String, Quest> getAllQuests() {
        return Collections.unmodifiableMap(quests);
    }
}
