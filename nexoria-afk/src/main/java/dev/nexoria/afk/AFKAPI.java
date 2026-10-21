package dev.nexoria.afk;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core AFK tracking and status management.
 * Thread-safe for Paper and Folia environments.
 */
public class AFKAPI {

    public record ServiceHealth(String service, boolean active, int totalOnline, int afkCount, Instant timestamp) {}

    private final NexoriaAFKPlugin plugin;
    private final Map<UUID, Long> lastActivityTimes = new ConcurrentHashMap<>();
    private final Set<UUID> afkPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<UUID, String> originalTabNames = new ConcurrentHashMap<>();
    private volatile boolean active;

    private int autoAfkSeconds;
    private String tabSuffix;
    private boolean autoKickEnabled;
    private int autoKickSeconds;
    private String kickMessage;

    public AFKAPI(NexoriaAFKPlugin plugin) {
        this.plugin = plugin;
        this.active = true;
        reload();
    }

    public void reload() {
        FileConfiguration config = plugin.getConfig();
        this.autoAfkSeconds = config.getInt("afk.auto-afk-seconds", 300);
        this.tabSuffix = config.getString("afk.tab-suffix", " &8[&7AFK&8]");
        this.autoKickEnabled = config.getBoolean("auto-kick.enabled", false);
        this.autoKickSeconds = config.getInt("auto-kick.kick-seconds", 1800);
        this.kickMessage = config.getString("auto-kick.kick-message", "&cYou have been kicked for idling too long.");
    }

    public boolean isActive() {
        return active;
    }

    public ServiceHealth getStatus() {
        int online = plugin.getServer().getOnlinePlayers().size();
        return new ServiceHealth("NexoriaAFK", active, online, afkPlayers.size(), Instant.now());
    }

    public boolean isAFK(UUID uuid) {
        return afkPlayers.contains(uuid);
    }

    public void recordActivity(Player player) {
        UUID uuid = player.getUniqueId();
        lastActivityTimes.put(uuid, System.currentTimeMillis());
        if (isAFK(uuid)) {
            setAFK(player, false, false);
        }
    }

    public void setAFK(Player player, boolean afk, boolean manual) {
        UUID uuid = player.getUniqueId();
        if (afk) {
            if (afkPlayers.add(uuid)) {
                String oldTab = player.getPlayerListName();
                originalTabNames.put(uuid, oldTab);
                String newTab = oldTab + ChatColor.translateAlternateColorCodes('&', tabSuffix);
                player.setPlayerListName(newTab);

                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.now-afk", "&7You are now &eAFK&7.")));

                String bcast = plugin.getConfig().getString("messages.broadcast-afk", "&e{player} &7is now &eAFK&7.");
                plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                        bcast.replace("{player}", player.getName())));
            }
        } else {
            if (afkPlayers.remove(uuid)) {
                String oldTab = originalTabNames.remove(uuid);
                if (oldTab != null) {
                    player.setPlayerListName(oldTab);
                } else {
                    player.setPlayerListName(player.getName());
                }

                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.no-longer-afk", "&7You are no longer &aAFK&7.")));

                String bcast = plugin.getConfig().getString("messages.broadcast-return", "&e{player} &7is no longer &aAFK&7.");
                plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                        bcast.replace("{player}", player.getName())));
            }
        }
    }

    public void checkAllPlayers() {
        if (!active) return;
        long now = System.currentTimeMillis();

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            UUID uuid = p.getUniqueId();
            Long last = lastActivityTimes.get(uuid);
            if (last == null) {
                lastActivityTimes.put(uuid, now);
                continue;
            }

            long idleSeconds = (now - last) / 1000;

            // Check auto AFK
            if (!isAFK(uuid) && autoAfkSeconds > 0 && idleSeconds >= autoAfkSeconds) {
                setAFK(p, true, false);
            }

            // Check auto kick
            if (autoKickEnabled && isAFK(uuid) && autoKickSeconds > 0 && idleSeconds >= autoKickSeconds) {
                if (!p.hasPermission("nexoria.afk.bypass.kick")) {
                    p.kickPlayer(ChatColor.translateAlternateColorCodes('&', kickMessage));
                }
            }
        }
    }

    public Set<UUID> getAfkPlayers() {
        return Collections.unmodifiableSet(afkPlayers);
    }

    public void registerSession(UUID uuid) {
        lastActivityTimes.put(uuid, System.currentTimeMillis());
    }

    public void invalidateSession(UUID uuid) {
        lastActivityTimes.remove(uuid);
        afkPlayers.remove(uuid);
        originalTabNames.remove(uuid);
    }

    protected void shutdown() {
        this.active = false;
        lastActivityTimes.clear();
        afkPlayers.clear();
        originalTabNames.clear();
    }
}
