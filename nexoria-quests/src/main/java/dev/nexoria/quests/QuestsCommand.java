package dev.nexoria.quests;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public final class QuestsCommand extends Command implements BasicCommand {

    private final NexoriaQuestsPlugin plugin;

    public QuestsCommand(NexoriaQuestsPlugin plugin) {
        super("quests", "View and track active quests", "/quests [reload]", List.of("quest", "q"));
        this.plugin = plugin;
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
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("nexoria.quests.admin")) {
                sender.sendMessage("§cYou lack permission: nexoria.quests.admin");
                return true;
            }
            plugin.reloadConfig();
            plugin.getQuestManager().loadQuests();
            sender.sendMessage("§a[NexoriaQuests] Quests configuration reloaded.");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly in-game players can view the quest book.");
            return true;
        }

        Collection<Quest> all = plugin.getQuestManager().getAllQuests().values();
        player.sendMessage("§8================ §6Server Quests §8================");
        if (all.isEmpty()) {
            player.sendMessage("§7No active quests configured right now.");
        } else {
            for (Quest q : all) {
                QuestProgress prog = plugin.getQuestManager().getProgress(player.getUniqueId(), q.getId());
                String status = prog.isCompleted() ? "§a[COMPLETED]" : ("§e[" + prog.getCurrentProgress() + "/" + q.getRequiredAmount() + "]");
                player.sendMessage("§6• " + q.getName() + " " + status);
                player.sendMessage("  §7" + q.getDescription() + " §8(Reward: §a" + q.getRewardXp() + " XP§8, §e$" + String.format("%.2f", q.getRewardMoney()) + "§8)");
            }
        }
        player.sendMessage("§8=================================================");
        return true;
    }

    private List<String> getSuggestions(CommandSender sender, String[] args) {
        if (args.length <= 1 && sender.hasPermission("nexoria.quests.admin")) {
            return List.of("reload");
        }
        return List.of();
    }
}
