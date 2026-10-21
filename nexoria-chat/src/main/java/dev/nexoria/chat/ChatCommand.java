package dev.nexoria.chat;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

/**
 * Dual Paper-Brigadier and Bukkit command implementation for NexoriaChat.
 * Handles /chat commands with subcommands: reload, mute, clear, filter, status, help.
 */
public final class ChatCommand extends Command implements BasicCommand {

    private final NexoriaChatPlugin plugin;

    public ChatCommand(NexoriaChatPlugin plugin) {
        super("chat", "Primary command entrypoint for NexoriaChat", "/chat [help|status|reload|mute|clear|filter]", List.of("nexoriachat"));
        this.plugin = plugin;
        setPermission("nexoria.chat.use");
    }

    // --- Paper BasicCommand Implementation ---

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        handleExecution(stack.getSender(), args);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        return getSuggestions(args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("nexoria.chat.use");
    }

    @Override
    public String permission() {
        return "nexoria.chat.use";
    }

    // --- Bukkit Command Fallback Implementation ---

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return handleExecution(sender, args);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return getSuggestions(args);
    }

    // --- Unified Command Logic ---

    private boolean handleExecution(CommandSender sender, String[] args) {
        ChatAPI api = plugin.getApi();
        if (api == null) {
            sender.sendMessage("§e[NexoriaChat] Chat service is still initializing...");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§8================ §dNexoriaChat Help §8================");
            sender.sendMessage("§7• §d/chat status §8- §7Check chat engine status and filter metrics.");
            if (sender.hasPermission("nexoria.chat.admin")) {
                sender.sendMessage("§7• §d/chat mute §8- §7Toggle global chat silence.");
                sender.sendMessage("§7• §d/chat clear §8- §7Clear chat history for all players.");
                sender.sendMessage("§7• §d/chat filter add <word> §8- §7Add a word to blocked list.");
                sender.sendMessage("§7• §d/chat filter remove <word> §8- §7Remove a word from blocked list.");
                sender.sendMessage("§7• §d/chat filter list §8- §7View all active blocked words.");
                sender.sendMessage("§7• §d/chat reload §8- §7Reload chat format and configuration.");
            }
            sender.sendMessage("§8=====================================================");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("status")) {
            var status = api.getStatus();
            sender.sendMessage("§8================ §dNexoriaChat Status §8================");
            sender.sendMessage("§7• Operational State: " + (status.active() ? "§aHealthy & Active" : "§cSuspended"));
            sender.sendMessage("§7• Global Mute: " + (status.chatMuted() ? "§cActive (Muted)" : "§aInactive (Open)"));
            sender.sendMessage("§7• Filtered Words: §f" + status.filteredWordsCount() + " §7rules");
            sender.sendMessage("§7• Platform: §f" + plugin.getServer().getName() + " on Java 25");
            sender.sendMessage("§8=========================================================");
            return true;
        }

        if (!sender.hasPermission("nexoria.chat.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission", "&cYou lack permission: nexoria.chat.admin")));
            return true;
        }

        switch (sub) {
            case "reload" -> {
                plugin.reloadConfig();
                api.reload();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.reloaded", "&a[NexoriaChat] Configuration successfully reloaded.")));
            }
            case "mute" -> {
                boolean newMuted = !api.isChatMuted();
                api.setChatMuted(newMuted);
                String msgKey = newMuted ? "messages.chat-muted" : "messages.chat-unmuted";
                String def = newMuted ? "&cGlobal chat has been muted by staff." : "&aGlobal chat has been unmuted.";
                plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString(msgKey, def)));
            }
            case "clear" -> {
                String by = (sender instanceof Player p) ? p.getName() : "Console";
                api.clearChat(by);
            }
            case "filter" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /chat filter <add|remove|list> [word]");
                    return true;
                }
                String action = args[1].toLowerCase();
                if (action.equals("list")) {
                    sender.sendMessage("§8[§dNexoriaChat§8] §7Blocked words (" + api.getBlockedWords().size() + "): §f" +
                            String.join(", ", api.getBlockedWords()));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /chat filter " + action + " <word>");
                    return true;
                }
                String targetWord = args[2].toLowerCase();
                if (action.equals("add")) {
                    boolean ok = api.addBlockedWord(targetWord);
                    if (ok) {
                        sender.sendMessage("§a[NexoriaChat] Added '" + targetWord + "' to blocked filter.");
                    } else {
                        sender.sendMessage("§e[NexoriaChat] Word '" + targetWord + "' is already blocked.");
                    }
                } else if (action.equals("remove")) {
                    boolean ok = api.removeBlockedWord(targetWord);
                    if (ok) {
                        sender.sendMessage("§a[NexoriaChat] Removed '" + targetWord + "' from blocked filter.");
                    } else {
                        sender.sendMessage("§c[NexoriaChat] Word '" + targetWord + "' was not found in blocked list.");
                    }
                }
            }
            default -> sender.sendMessage("§cUnknown subcommand. Use §f/chat help");
        }

        return true;
    }

    private List<String> getSuggestions(String[] args) {
        if (args.length <= 1) {
            return List.of("help", "status", "mute", "clear", "filter", "reload");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("filter")) {
            return List.of("add", "remove", "list");
        }
        return List.of();
    }
}
