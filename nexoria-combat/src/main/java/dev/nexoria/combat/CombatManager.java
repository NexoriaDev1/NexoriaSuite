package dev.nexoria.combat;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatManager {

    private final NexoriaCombatPlugin plugin;
    private final Map<UUID, Long> combatExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, String> lastAttacker = new ConcurrentHashMap<>();
    private final Map<UUID, Long> pearlCooldowns = new ConcurrentHashMap<>();
    private BukkitTask tickerTask;

    public CombatManager(NexoriaCombatPlugin plugin) {
        this.plugin = plugin;
        startTicker();
    }

    private void startTicker() {
        this.tickerTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            boolean actionBar = plugin.getConfig().getBoolean("settings.actionbar-timer", true);
            String abFormat = plugin.getConfig().getString("settings.actionbar-format", "&cIn combat: &e{time}s &7| &cDo not disconnect!");
            long now = System.currentTimeMillis();

            for (Map.Entry<UUID, Long> entry : combatExpiry.entrySet()) {
                UUID uuid = entry.getKey();
                long expire = entry.getValue();
                Player p = Bukkit.getPlayer(uuid);

                if (now >= expire) {
                    combatExpiry.remove(uuid);
                    lastAttacker.remove(uuid);
                    if (p != null && p.isOnline()) {
                        String msg = plugin.getConfig().getString("messages.tag-expired", "&aYou are no longer in combat.");
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&cCombat&8] ") + msg));
                    }
                } else if (actionBar && p != null && p.isOnline()) {
                    long remaining = Math.max(1, (expire - now) / 1000L);
                    String text = ChatColor.translateAlternateColorCodes('&', abFormat.replace("{time}", String.valueOf(remaining)));
                    p.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(text));
                }
            }
        }, 20L, 20L);
    }

    public void tag(Player victim, Player attacker) {
        if (victim.hasPermission("nexoria.combat.bypass") || attacker.hasPermission("nexoria.combat.bypass")) {
            return;
        }

        int durationSec = Math.max(3, plugin.getConfig().getInt("settings.combat-tag-seconds", 15));
        long expireTime = System.currentTimeMillis() + (durationSec * 1000L);

        boolean victimWasTagged = isTagged(victim.getUniqueId());
        boolean attackerWasTagged = isTagged(attacker.getUniqueId());

        combatExpiry.put(victim.getUniqueId(), expireTime);
        lastAttacker.put(victim.getUniqueId(), attacker.getName());

        combatExpiry.put(attacker.getUniqueId(), expireTime);
        lastAttacker.put(attacker.getUniqueId(), victim.getName());

        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&cCombat&8] ");

        if (!victimWasTagged) {
            String msg = plugin.getConfig().getString("messages.tagged", "&cYou are now in combat with &e{enemy}&c!");
            victim.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg.replace("{enemy}", attacker.getName()).replace("{time}", String.valueOf(durationSec))));
        }

        if (!attackerWasTagged) {
            String msg = plugin.getConfig().getString("messages.tagged", "&cYou are now in combat with &e{enemy}&c!");
            attacker.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg.replace("{enemy}", victim.getName()).replace("{time}", String.valueOf(durationSec))));
        }
    }

    public boolean isTagged(UUID uuid) {
        Long expire = combatExpiry.get(uuid);
        return expire != null && System.currentTimeMillis() < expire;
    }

    public int getRemainingSeconds(UUID uuid) {
        Long expire = combatExpiry.get(uuid);
        if (expire == null) return 0;
        long diff = expire - System.currentTimeMillis();
        return diff > 0 ? (int) (diff / 1000L) : 0;
    }

    public String getLastEnemy(UUID uuid) {
        return lastAttacker.getOrDefault(uuid, "an enemy");
    }

    public void untag(UUID uuid) {
        combatExpiry.remove(uuid);
        lastAttacker.remove(uuid);
    }

    public void handleLogout(Player player) {
        if (!isTagged(player.getUniqueId())) {
            return;
        }

        String enemy = getLastEnemy(player.getUniqueId());
        untag(player.getUniqueId());

        if (plugin.getConfig().getBoolean("settings.kill-on-logout", true)) {
            player.setHealth(0.0);
        }

        if (plugin.getConfig().getBoolean("settings.broadcast-logout", true)) {
            String format = plugin.getConfig().getString("messages.logout-broadcast", "&c{player} logged out while fighting {enemy} and was killed!");
            String msg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&cCombat&8] ") + format.replace("{player}", player.getName()).replace("{enemy}", enemy));
            Bukkit.broadcastMessage(msg);
        }
    }

    public boolean checkPearlCooldown(Player player) {
        int cdSec = plugin.getConfig().getInt("settings.enderpearl-cooldown-seconds", 10);
        if (cdSec <= 0 || player.hasPermission("nexoria.combat.bypass")) {
            return true;
        }

        long now = System.currentTimeMillis();
        Long expire = pearlCooldowns.get(player.getUniqueId());
        if (expire != null && now < expire) {
            long remaining = Math.max(1, (expire - now) / 1000L);
            String prefix = plugin.getConfig().getString("messages.prefix", "&8[&cCombat&8] ");
            String msg = plugin.getConfig().getString("messages.enderpearl-cooldown", "&cEnderpearl is on cooldown: &e{time}s");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg.replace("{time}", String.valueOf(remaining))));
            return false;
        }

        pearlCooldowns.put(player.getUniqueId(), now + (cdSec * 1000L));
        return true;
    }

    public void shutdown() {
        if (tickerTask != null) {
            tickerTask.cancel();
        }
        combatExpiry.clear();
        lastAttacker.clear();
        pearlCooldowns.clear();
    }
}
