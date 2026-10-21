package dev.nexoria.claims;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public final class ClaimsCommand extends Command implements BasicCommand {

    private final NexoriaClaimsPlugin plugin;
    private final String mode; // "claim", "unclaim", "trust", "untrust", "claimadmin"

    public ClaimsCommand(NexoriaClaimsPlugin plugin, String name, String mode, List<String> aliases) {
        super(name, "Claims management command", "/" + name, aliases);
        this.plugin = plugin;
        this.mode = mode;
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        handleExecution(stack.getSender(), args);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        return getSuggestions(stack.getSender(), args);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return handleExecution(sender, args);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return getSuggestions(sender, args);
    }

    private boolean handleExecution(CommandSender sender, String[] args) {
        ClaimManager mgr = plugin.getClaimManager();

        switch (mode) {
            case "claim" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly in-game players can claim chunks.");
                    return true;
                }

                if (args.length > 0) {
                    String sub = args[0].toLowerCase();
                    if (sub.equals("info")) {
                        showInfo(player, mgr.getClaim(player.getLocation()));
                        return true;
                    }
                    if (sub.equals("list")) {
                        showList(player);
                        return true;
                    }
                }

                Chunk chunk = player.getLocation().getChunk();
                if (mgr.isClaimed(chunk)) {
                    Claim c = mgr.getClaim(chunk);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.already-claimed", "&cThis chunk is already claimed by &e" + c.getOwnerName() + "&c.")));
                    return true;
                }

                if (!mgr.canClaim(player, chunk)) {
                    int max = mgr.getMaxClaims(player);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.limit-reached", "&cYou reached your maximum claim limit of &e" + max + " &cchunks.")));
                    return true;
                }

                Claim newClaim = mgr.createClaim(player, chunk);
                int count = mgr.getPlayerClaimCount(player.getUniqueId());
                int max = mgr.getMaxClaims(player);
                String msg = plugin.getConfig().getString("messages.claimed", "&aSuccessfully claimed this chunk! &8(&7{current}/{max}&8)");
                msg = msg.replace("{current}", String.valueOf(count)).replace("{max}", String.valueOf(max));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }

            case "unclaim" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly in-game players can unclaim chunks.");
                    return true;
                }
                Chunk chunk = player.getLocation().getChunk();
                Claim claim = mgr.getClaim(chunk);
                if (claim == null) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.not-claimed", "&cThis chunk is not claimed.")));
                    return true;
                }
                if (!claim.getOwner().equals(player.getUniqueId()) && !player.hasPermission("nexoria.claims.admin")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.not-owner", "&cYou do not own this claim.")));
                    return true;
                }
                mgr.unclaim(chunk.getWorld(), chunk.getX(), chunk.getZ());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.unclaimed", "&aChunk has been unclaimed.")));
                return true;
            }

            case "trust" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly in-game players can manage trust.");
                    return true;
                }
                if (args.length < 1) {
                    player.sendMessage("§cUsage: /trust <player>");
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                if (target == null || target.getName() == null) {
                    player.sendMessage("§cPlayer not found: " + args[0]);
                    return true;
                }
                if (target.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage("§cYou cannot trust yourself.");
                    return true;
                }
                List<Claim> playerClaims = mgr.getClaims(player.getUniqueId());
                if (playerClaims.isEmpty()) {
                    player.sendMessage("§cYou do not own any land claims yet. Use /claim first.");
                    return true;
                }
                for (Claim c : playerClaims) {
                    c.addTrusted(target.getUniqueId());
                }
                mgr.saveClaims();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&aTrusted &e" + target.getName() + " &ain all your claims."));
                return true;
            }

            case "untrust" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly in-game players can manage trust.");
                    return true;
                }
                if (args.length < 1) {
                    player.sendMessage("§cUsage: /untrust <player>");
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                if (target == null || target.getName() == null) {
                    player.sendMessage("§cPlayer not found: " + args[0]);
                    return true;
                }
                List<Claim> playerClaims = mgr.getClaims(player.getUniqueId());
                for (Claim c : playerClaims) {
                    c.removeTrusted(target.getUniqueId());
                }
                mgr.saveClaims();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&aRemoved &e" + target.getName() + " &afrom your trusted players."));
                return true;
            }

            case "claimadmin" -> {
                if (!sender.hasPermission("nexoria.claims.admin")) {
                    sender.sendMessage("§cYou lack permission: nexoria.claims.admin");
                    return true;
                }
                if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                    sender.sendMessage("§8[§bNexoriaClaims§8] §7Admin commands: §f/claimadmin reload§7, §f/claimadmin unclaim§7, §f/claimadmin list");
                    return true;
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    plugin.reloadConfig();
                    mgr.loadClaims();
                    sender.sendMessage("§a[NexoriaClaims] Configuration and claims reloaded.");
                    return true;
                }
                if (args[0].equalsIgnoreCase("unclaim")) {
                    if (sender instanceof Player p) {
                        Chunk c = p.getLocation().getChunk();
                        boolean ok = mgr.unclaim(c.getWorld(), c.getX(), c.getZ());
                        if (ok) {
                            sender.sendMessage("§a[NexoriaClaims] Force-unclaimed current chunk.");
                        } else {
                            sender.sendMessage("§c[NexoriaClaims] Chunk was not claimed.");
                        }
                    } else {
                        sender.sendMessage("§cRun this command in-game.");
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("list")) {
                    sender.sendMessage("§8[§bNexoriaClaims§8] §7Total active claims: §f" + mgr.getAllClaims().size());
                    return true;
                }
            }
        }
        return true;
    }

    private void showInfo(Player player, Claim claim) {
        if (claim == null) {
            player.sendMessage("§8[§bNexoriaClaims§8] §7This chunk is §aunclaimed wilderness§7.");
            return;
        }
        player.sendMessage("§8================ §bClaim Details §8================");
        player.sendMessage("§7• Owner: §f" + claim.getOwnerName());
        player.sendMessage("§7• Location: §f" + claim.getWorld() + " §8[" + claim.getChunkX() + ", " + claim.getChunkZ() + "]");
        player.sendMessage("§7• Trusted: §f" + claim.getTrustedUsers().size() + " players");
        player.sendMessage("§7• PvP: " + (claim.isPvp() ? "§aAllowed" : "§cDenied"));
        player.sendMessage("§7• Explosions: " + (claim.isExplosions() ? "§aAllowed" : "§cDenied"));
        player.sendMessage("§8=================================================");
    }

    private void showList(Player player) {
        List<Claim> claims = plugin.getClaimManager().getClaims(player.getUniqueId());
        int max = plugin.getClaimManager().getMaxClaims(player);
        player.sendMessage("§8================ §bYour Land Claims (" + claims.size() + "/" + max + ") §8================");
        if (claims.isEmpty()) {
            player.sendMessage("§7You do not have any claimed chunks yet.");
        } else {
            for (Claim c : claims) {
                player.sendMessage("§7• Chunk §f[" + c.getChunkX() + ", " + c.getChunkZ() + "] §7in world §b" + c.getWorld());
            }
        }
        player.sendMessage("§8===========================================================");
    }

    private List<String> getSuggestions(CommandSender sender, String[] args) {
        if (mode.equals("claim") && args.length <= 1) {
            return List.of("info", "list");
        }
        if ((mode.equals("trust") || mode.equals("untrust")) && args.length == 1) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getName().equalsIgnoreCase(sender.getName())) {
                    names.add(p.getName());
                }
            }
            return names;
        }
        if (mode.equals("claimadmin") && args.length <= 1) {
            return List.of("reload", "unclaim", "list");
        }
        return List.of();
    }
}
