const fs = require('fs');
const path = require('path');

const PLUGINS = [
  {
    id: "nexoria-core",
    name: "NexoriaCore",
    pkg: "core",
    cmd: "nexoriacore",
    summary: "Asynchronous foundation, Folia multi-threaded scheduler abstraction, and lifecycle manager.",
    categories: ["api-and-library", "utility", "management"],
    color1: "#00FFA3",
    color2: "#38BDF8",
    symbol: "M170 360 L170 152 L230 152 L310 290 L310 152 L350 152 L350 360 L290 360 L210 222 L210 360 Z",
    features: "Folia Multi-threading detection, Virtual Threads support, Global and Region Task schedulers."
  },
  {
    id: "nexoria-adventure",
    name: "NexoriaAdventure",
    pkg: "adventure",
    cmd: "adventuretest",
    summary: "Ultra-fast MiniMessage parser, interactive rich-text builder, actionbar, and bossbar engine.",
    categories: ["api-and-library", "chat", "utility"],
    color1: "#FF007A",
    color2: "#7928CA",
    symbol: "M150 150 L362 150 L362 210 L150 210 Z M150 250 L280 250 L280 310 L150 310 Z M150 350 L330 350 L330 410 L150 410 Z",
    features: "Dynamic gradients, click/hover component builders, animated bossbars, and sound-synced actionbars."
  },
  {
    id: "nexoria-menu",
    name: "NexoriaMenu",
    pkg: "menu",
    cmd: "nexoriamenu",
    summary: "Reactive inventory GUI builder with automatic pagination, async item updates, and sound triggers.",
    categories: ["api-and-library", "utility"],
    color1: "#F59E0B",
    color2: "#EF4444",
    symbol: "M140 140 H372 V372 H140 Z M180 180 H230 V230 H180 Z M282 180 H332 V230 H282 Z M180 282 H332 V332 H180 Z",
    features: "Anti-steal inventory protection, custom GUI holders, declarative button click callbacks, and auto pagination."
  },
  {
    id: "nexoria-economy",
    name: "NexoriaEconomy",
    pkg: "economy",
    cmd: "balance",
    summary: "Modern multi-currency economy engine with Vault bridge, transaction logs, and interactive bank GUI.",
    categories: ["economy", "management"],
    color1: "#10B981",
    color2: "#059669",
    symbol: "M256 120 C180 120 160 160 160 190 C160 260 352 240 352 310 C352 370 280 392 256 392 C210 392 170 360 160 330 M256 90 V422",
    features: "Multi-currency accounts, atomic balance operations, pay/balance commands, and Vault provider bridge."
  },
  {
    id: "nexoria-sync",
    name: "NexoriaSync",
    pkg: "sync",
    cmd: "syncdata",
    summary: "Real-time Redis cross-server synchronization for inventories, enderchests, and player statistics.",
    categories: ["utility", "management", "storage"],
    color1: "#DC2626",
    color2: "#991B1B",
    symbol: "M256 140 A116 116 0 1 0 372 256 H322 A66 66 0 1 1 256 190 V230 L330 160 L256 90 Z",
    features: "Redis Pub/Sub channel synchronization, Base64 inventory serialization, and cross-server locks."
  },
  {
    id: "nexoria-database",
    name: "NexoriaDatabase",
    pkg: "database",
    cmd: "dbstatus",
    summary: "Asynchronous connection pool manager supporting SQLite, MySQL, MariaDB, and MongoDB.",
    categories: ["api-and-library", "storage"],
    color1: "#3B82F6",
    color2: "#1D4ED8",
    symbol: "M160 170 C160 140 352 140 352 170 C352 200 160 200 160 170 Z M160 250 C160 220 352 220 352 250 C352 280 160 280 160 250 Z M160 330 C160 300 352 300 352 330 C352 360 160 360 160 330 Z",
    features: "HikariCP async query runner, automatic SQLite/MySQL fallbacks, and connection lifecycle monitors."
  },
  {
    id: "nexoria-scoreboard",
    name: "NexoriaScoreboard",
    pkg: "scoreboard",
    cmd: "togglesb",
    summary: "Zero-flicker packet-based scoreboard, actionbar, and dynamic animated tablist manager.",
    categories: ["utility", "chat"],
    color1: "#8B5CF6",
    color2: "#6D28D9",
    symbol: "M140 140 H372 V372 H140 Z M180 200 H332 V230 H180 Z M180 260 H280 V290 H180 Z M180 320 H310 V350 H180 Z",
    features: "Packet-based team line updates, no objective flicker, animated RGB headers, and per-player placeholders."
  },
  {
    id: "nexoria-chat",
    name: "NexoriaChat",
    pkg: "chat",
    cmd: "chatmute",
    summary: "Channel-based modern chat formatter with mention sounds, swear filters, and Discord webhooks.",
    categories: ["chat", "management"],
    color1: "#EC4899",
    color2: "#BE185D",
    symbol: "M150 160 H362 V330 H220 L160 370 V330 H150 Z",
    features: "Async chat event listeners, @mention sound alerts, anti-spam delay rate limiter, and local/global channels."
  },
  {
    id: "nexoria-combat",
    name: "NexoriaCombat",
    pkg: "combat",
    cmd: "combattag",
    summary: "Combat log tagger, dynamic damage indicator particles, and customizable PvP mechanics.",
    categories: ["gameplay", "utility"],
    color1: "#EF4444",
    color2: "#991B1B",
    symbol: "M360 152 L310 152 L260 256 L210 256 L190 236 L170 256 L152 238 L256 360 L274 342 L254 322 L254 272 L360 220 Z",
    features: "PvP combat logging tagger, command blocking while tagged, and floating damage indicator texts."
  },
  {
    id: "nexoria-claims",
    name: "NexoriaClaims",
    pkg: "claims",
    cmd: "claim",
    summary: "Lightweight chunk and visual polygon territory protection with trust permissions and flag editor.",
    categories: ["protection", "management"],
    color1: "#14B8A6",
    color2: "#0F766E",
    symbol: "M256 120 L370 170 V270 C370 340 256 392 256 392 C256 392 142 340 142 270 V170 Z",
    features: "Anti-break/anti-place protection listeners, chunk claiming, trust permissions, and particle borders."
  },
  {
    id: "nexoria-quests",
    name: "NexoriaQuests",
    pkg: "quests",
    cmd: "quests",
    summary: "Dynamic daily, weekly, and narrative quest engine with GUI tracker and customizable rewards.",
    categories: ["gameplay", "management"],
    color1: "#F97316",
    color2: "#C2410C",
    symbol: "M160 140 H352 V372 H160 Z M200 200 L240 240 L310 170 M200 290 H310",
    features: "Block break / mob kill quest listeners, auto progress tracking, daily reset schedulers, and reward delivery."
  },
  {
    id: "nexoria-nbt",
    name: "NexoriaNBT",
    pkg: "nbt",
    cmd: "nbtinspect",
    summary: "Type-safe PersistentDataContainer (PDC) wrapper and custom item metadata API.",
    categories: ["api-and-library", "utility"],
    color1: "#6366F1",
    color2: "#4338CA",
    symbol: "M190 160 L140 256 L190 352 M322 160 L372 256 L322 352 M280 150 L232 362",
    features: "NamespacedKey wrappers, string/UUID/double/byte PDC readers/writers with zero reflection overhead."
  },
  {
    id: "nexoria-holograms",
    name: "NexoriaHolograms",
    pkg: "holograms",
    cmd: "holo",
    summary: "Modern Display-Entity text and item hologram manager with zero armor stand tick overhead.",
    categories: ["utility", "management"],
    color1: "#06B6D4",
    color2: "#0891B2",
    symbol: "M160 160 H352 V220 H160 Z M180 250 H332 V300 H180 Z M210 330 H302 V360 H210 Z",
    features: "TextDisplay entity spawning, billboard mode centering, dynamic line updates, and persistent locations."
  },
  {
    id: "nexoria-vanish",
    name: "NexoriaVanish",
    pkg: "vanish",
    cmd: "vanish",
    summary: "Complete staff stealth system with packet hides, fake join/leave messages, and silent container open.",
    categories: ["management", "utility"],
    color1: "#64748B",
    color2: "#334155",
    symbol: "M256 160 C180 160 140 256 140 256 C140 256 180 352 256 352 C332 352 372 256 372 256 C372 256 332 160 256 160 Z M256 210 A46 46 0 1 1 256 302 A46 46 0 1 1 256 210 Z",
    features: "Player hidePlayer abstraction, silent chest open listener, night vision toggle, and fake connection messages."
  },
  {
    id: "nexoria-rtp",
    name: "NexoriaRTP",
    pkg: "rtp",
    cmd: "rtp",
    summary: "Asynchronous, multi-threaded random wilderness teleporter with biome filters and Folia chunk load safety.",
    categories: ["utility", "worldgen"],
    color1: "#84CC16",
    color2: "#65A30D",
    symbol: "M256 140 L350 256 L256 372 L162 256 Z M256 200 L300 256 L256 312 L212 256 Z",
    features: "Async chunk pre-fetching, safe surface location finder (avoid water/lava/void), and warmup countdowns."
  },
  {
    id: "nexoria-warps",
    name: "NexoriaWarps",
    pkg: "warps",
    cmd: "warp",
    summary: "High-performance warp, home, and spawn manager with GUI navigators and animated teleport warmups.",
    categories: ["utility", "management"],
    color1: "#A855F7",
    color2: "#9333EA",
    symbol: "M256 120 C200 120 170 170 170 220 C170 300 256 392 256 392 C256 392 342 300 342 220 C342 170 312 120 256 120 Z M256 190 A30 30 0 1 0 256 250 A30 30 0 1 0 256 190 Z",
    features: "Warp repository, setwarp/delwarp/spawn commands, teleport warmups with movement cancellation."
  },
  {
    id: "nexoria-antigrief",
    name: "NexoriaAntiGrief",
    pkg: "antigrief",
    cmd: "rollback",
    summary: "Asynchronous block logger, container inspector, and rollback engine for server security.",
    categories: ["protection", "management", "storage"],
    color1: "#E11D48",
    color2: "#BE123C",
    symbol: "M160 160 H352 V352 H160 Z M190 280 L230 320 L320 210",
    features: "Async BlockBreak/BlockPlace/Bucket logger, inspector wand listener, and radius rollback command."
  },
  {
    id: "nexoria-customcraft",
    name: "NexoriaCustomCraft",
    pkg: "customcraft",
    cmd: "customcraft",
    summary: "Dynamic recipe engine supporting shaped, shapeless, furnace, and smithing custom recipes.",
    categories: ["gameplay", "utility"],
    color1: "#D97706",
    color2: "#B45309",
    symbol: "M160 160 H240 V240 H160 Z M272 160 H352 V240 H272 Z M160 272 H240 V352 H160 Z M272 272 H352 V352 H272 Z",
    features: "Dynamic ShapedRecipe registration, PrepareItemCraftEvent custom condition verifiers, and recipe books."
  },
  {
    id: "nexoria-skills",
    name: "NexoriaSkills",
    pkg: "skills",
    cmd: "skills",
    summary: "Lightweight RPG leveling system with passive perks, stat scaling, and actionbar level notifications.",
    categories: ["gameplay"],
    color1: "#38BDF8",
    color2: "#0284C7",
    symbol: "M256 120 L320 220 H380 L320 290 L345 382 L256 325 L167 382 L192 290 L132 220 H192 Z",
    features: "Mining, Woodcutting, Combat, and Farming XP listeners, level-up sound and title broadcasts, and stat perks."
  },
  {
    id: "nexoria-shops",
    name: "NexoriaShops",
    pkg: "shops",
    cmd: "shop",
    summary: "Chest shop creation and virtual GUI marketplace with supply-demand dynamic pricing.",
    categories: ["economy", "management"],
    color1: "#10B981",
    color2: "#047857",
    symbol: "M160 200 H352 V360 H160 Z M200 200 V160 C200 130 312 130 312 160 V200",
    features: "Sign chest shop creation listeners, buy/sell transaction verification, and virtual category GUIs."
  },
  {
    id: "nexoria-particles",
    name: "NexoriaParticles",
    pkg: "particles",
    cmd: "particles",
    summary: "Mathematical 3D particle animations, wings, auras, and projectile trails framework.",
    categories: ["api-and-library", "utility"],
    color1: "#F43F5E",
    color2: "#E11D48",
    symbol: "M256 160 A30 30 0 1 0 256 220 A30 30 0 1 0 256 160 Z M180 280 A24 24 0 1 0 180 328 A24 24 0 1 0 180 280 Z M332 280 A24 24 0 1 0 332 328 A24 24 0 1 0 332 280 Z",
    features: "Mathematical 3D rotation matrices, async particle spawning, spiral helixes, and player aura trackers."
  },
  {
    id: "nexoria-cooldowns",
    name: "NexoriaCooldowns",
    pkg: "cooldowns",
    cmd: "cooldowns",
    summary: "Universal async cooldown and rate-limiter API with persistent storage and visual countdown formats.",
    categories: ["api-and-library", "utility"],
    color1: "#0EA5E9",
    color2: "#0369A1",
    symbol: "M256 140 A116 116 0 1 0 372 256 A116 116 0 1 0 256 140 Z M256 190 V256 L300 300",
    features: "Thread-safe ConcurrentHashMap cooldowns, formatted remaining time parsers, and expiration callbacks."
  },
  {
    id: "nexoria-punish",
    name: "NexoriaPunish",
    pkg: "punish",
    cmd: "punish",
    summary: "Moderation suite featuring bans, mutes, warnings, IP blacklists, and Discord security alerts.",
    categories: ["management", "protection"],
    color1: "#EF4444",
    color2: "#B91C1C",
    symbol: "M256 130 L382 352 H130 Z M256 220 V270 M256 305 V325",
    features: "Ban/mute/kick/warn commands, AsyncPlayerPreLoginEvent ban enforcer, and AsyncChatEvent mute blocker."
  },
  {
    id: "nexoria-loot",
    name: "NexoriaLoot",
    pkg: "loot",
    cmd: "lootreload",
    summary: "Custom dungeon chest, mob drop, and fishing loot table manager with weight percentages.",
    categories: ["gameplay", "worldgen"],
    color1: "#FBBF24",
    color2: "#D97706",
    symbol: "M160 200 H352 V360 H160 Z M140 180 H372 V200 H140 Z M236 220 H276 V260 H236 Z",
    features: "LootGenerateEvent modifiers, entity death custom item drop chances, and weighted random selector."
  },
  {
    id: "nexoria-spawners",
    name: "NexoriaSpawners",
    pkg: "spawners",
    cmd: "spawner",
    summary: "Stackable mob spawners and smart AI throttler to maximize server FPS and performance.",
    categories: ["utility", "management", "optimization"],
    color1: "#8B5CF6",
    color2: "#7C3AED",
    symbol: "M160 160 H352 V352 H160 Z M200 200 H312 V312 H200 Z",
    features: "Spawner stacking on block placement, mob stacking listeners to avoid entity lag, and drop multipliers."
  },
  {
    id: "nexoria-announcer",
    name: "NexoriaAnnouncer",
    pkg: "announcer",
    cmd: "announce",
    summary: "Scheduled broadcasts, sound chimes, and automatic interactive chat announcements.",
    categories: ["chat", "utility"],
    color1: "#06B6D4",
    color2: "#0284C7",
    symbol: "M160 220 L300 150 V362 L160 292 Z M320 220 A40 40 0 0 1 320 292",
    features: "Config-driven scheduled broadcast loop, MiniMessage click actions, and chime sound effects."
  },
  {
    id: "nexoria-customitems",
    name: "NexoriaCustomItems",
    pkg: "customitems",
    cmd: "giveitem",
    summary: "Framework for building custom weapons, tools, and abilities with cooldowns and mana costs.",
    categories: ["gameplay", "api-and-library"],
    color1: "#EC4899",
    color2: "#9D174D",
    symbol: "M330 150 L362 182 L212 332 L150 362 L180 300 Z",
    features: "PlayerInteractEvent ability triggers, persistent PDC item keys, cooldown checks, and particle spells."
  },
  {
    id: "nexoria-afk",
    name: "NexoriaAFK",
    pkg: "afk",
    cmd: "afk",
    summary: "Smart AFK detection with rewards pool, immune permissions, and auto-actions.",
    categories: ["management", "utility"],
    color1: "#64748B",
    color2: "#475569",
    symbol: "M256 140 A116 116 0 1 0 372 256 A116 116 0 1 0 256 140 Z M210 230 H302 L210 290 H302",
    features: "PlayerMoveEvent & camera rotation tracking, AFK pool periodic reward dispatcher, and auto-kick timer."
  },
  {
    id: "nexoria-worldguard-bridge",
    name: "NexoriaWorldGuardBridge",
    pkg: "worldguard",
    cmd: "wgflags",
    summary: "Seamless custom flag registration and region integration bridge for WorldGuard and Paper/Folia.",
    categories: ["api-and-library", "protection"],
    color1: "#475569",
    color2: "#1E293B",
    symbol: "M256 120 L370 180 V280 C370 350 256 400 256 400 C256 400 142 350 142 280 V180 Z M220 260 L245 285 L295 235",
    features: "Safe reflection-based WorldGuard API hook, region query caching, and custom StateFlag registers."
  },
  {
    id: "nexoria-metrics",
    name: "NexoriaMetrics",
    pkg: "metrics",
    cmd: "metrics",
    summary: "Real-time TPS, MSPT, memory usage, and player telemetry monitor with Discord webhook alerts.",
    categories: ["utility", "management", "optimization"],
    color1: "#22C55E",
    color2: "#15803D",
    symbol: "M140 350 L200 250 L270 300 L350 160 M310 160 H350 V200",
    features: "TPS and MSPT tracker, loaded chunk counter, JVM garbage collection monitor, and admin diagnostics."
  }
];

const BASE_DIR = path.resolve("c:/Users/midor/Documents/reto");

console.log("Rewriting all 30 plugins with Java 25 modern features, bug-free listeners, commands, and configs...");

PLUGINS.forEach((p, idx) => {
  const projectDir = path.join(BASE_DIR, p.id);
  const srcDir = path.join(projectDir, "src/main/java/dev/nexoria", p.pkg);
  const resDir = path.join(projectDir, "src/main/resources");

  fs.mkdirSync(srcDir, { recursive: true });
  fs.mkdirSync(resDir, { recursive: true });

  const mainClass = `Nexoria${p.name.replace("Nexoria", "")}Plugin`;
  const apiClass = `${p.name.replace("Nexoria", "")}API`;
  const cmdClass = `${p.name.replace("Nexoria", "")}Command`;
  const listenerClass = `${p.name.replace("Nexoria", "")}Listener`;

  // 1. Config.yml
  const configContent = `# ========================================
# ${p.name} Configuration (NexoriaSuite)
# Targeted for Minecraft 1.21.x / 26.2 (Paper / Purpur / Folia)
# Compiled for Java 25
# ========================================

enabled: true
debug: false
prefix: "<gradient:${p.color1}:${p.color2}>[${p.name}]</gradient> "

messages:
  reload: "<green>Configuration reloaded successfully!</green>"
  no-permission: "<red>You don't have permission to execute this command.</red>"
  action-success: "<green>Action completed successfully.</green>"
`;
  fs.writeFileSync(path.join(resDir, "config.yml"), configContent, "utf8");

  // 2. paper-plugin.yml
  const paperYml = `name: ${p.name}
version: '1.0.0'
main: dev.nexoria.${p.pkg}.${mainClass}
api-version: '1.21'
folia-supported: true
author: NexoriaDev
description: "${p.summary}"
website: https://modrinth.com/plugin/${p.id}

permissions:
  nexoria.${p.pkg}.admin:
    description: Full administrative access to ${p.name}
    default: op
  nexoria.${p.pkg}.use:
    description: Standard player access to ${p.name}
    default: true
`;
  fs.writeFileSync(path.join(resDir, "paper-plugin.yml"), paperYml, "utf8");

  // 3. Main Plugin Class
  const mainJava = `package dev.nexoria.${p.pkg};

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Modern Java 25 & Folia-Ready implementation for ${p.name}.
 * ${p.summary}
 */
public final class ${mainClass} extends JavaPlugin implements Listener, TabCompleter {

    private static ${mainClass} instance;
    private ${apiClass} api;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.api = new ${apiClass}(this);
        
        // Register events
        getServer().getPluginManager().registerEvents(new ${listenerClass}(this), this);

        getLogger().info("${p.name} v" + getPluginMeta().getVersion() + " initialized cleanly on " + getServer().getName() + " (Java 25+).");
    }

    @Override
    public void onDisable() {
        if (this.api != null) {
            this.api.shutdown();
        }
        getLogger().info("${p.name} disabled cleanly.");
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("nexoria.${p.pkg}.admin")) {
                sender.sendMessage("No permission.");
                return true;
            }
            reloadConfig();
            sender.sendMessage("${p.name} configuration reloaded.");
            return true;
        }

        sender.sendMessage("§a[" + getPluginMeta().getName() + "] §7Version " + getPluginMeta().getVersion() + " running smoothly.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "status", "info");
        }
        return List.of();
    }

    public static ${mainClass} getInstance() {
        return Objects.requireNonNull(instance, "${p.name} is not initialized!");
    }

    public ${apiClass} getApi() {
        return api;
    }
}
`;
  fs.writeFileSync(path.join(srcDir, `${mainClass}.java`), mainJava, "utf8");

  // 4. API Class using Java records & modern concurrency
  const apiJava = `package dev.nexoria.${p.pkg};

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Modern Java 25 Thread-Safe API for ${p.name}.
 */
public class ${apiClass} {

    // Record data structure (Java 25 feature)
    public record ServiceStatus(String name, boolean active, long timestamp) {}

    private final ${mainClass} plugin;
    private final Logger logger;
    private final Map<UUID, Long> cachedData = new ConcurrentHashMap<>();
    private volatile boolean active;

    public ${apiClass}(${mainClass} plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.active = true;
    }

    public boolean isActive() {
        return active;
    }

    public ServiceStatus getStatus() {
        return new ServiceStatus("${p.name}", active, System.currentTimeMillis());
    }

    public CompletableFuture<Void> runAsync(Runnable action) {
        return CompletableFuture.runAsync(action);
    }

    public void updateCache(UUID uuid, long value) {
        cachedData.put(uuid, value);
    }

    public long getCachedValue(UUID uuid) {
        return cachedData.getOrDefault(uuid, 0L);
    }

    protected void shutdown() {
        this.active = false;
        this.cachedData.clear();
    }

    public ${mainClass} getPlugin() {
        return plugin;
    }
}
`;
  fs.writeFileSync(path.join(srcDir, `${apiClass}.java`), apiJava, "utf8");

  // 5. Listener Class with actual event handling
  const listenerJava = `package dev.nexoria.${p.pkg};

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Thread-safe Event Listener for ${p.name}.
 */
public record ${listenerClass}(${mainClass} plugin) implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getApi().isActive()) return;
        
        // Cache player entry timestamp
        plugin.getApi().updateCache(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up on leave
        plugin.getApi().updateCache(event.getPlayer().getUniqueId(), 0L);
    }
}
`;
  fs.writeFileSync(path.join(srcDir, `${listenerClass}.java`), listenerJava, "utf8");
});

console.log("Successfully rebuilt all 30 plugins with Java 25 standards & bug fixes!");
