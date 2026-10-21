package dev.nexoria.skills;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SkillsCommand extends Command implements BasicCommand {

    private final NexoriaSkillsPlugin plugin;

    public SkillsCommand(NexoriaSkillsPlugin plugin) {
        super("skills", "View player skills and progression", "/skills [help|reload]", List.of("skill", "sk"));
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
            if (!sender.hasPermission("nexoria.skills.admin")) {
                sender.sendMessage("§cYou lack permission: nexoria.skills.admin");
                return true;
            }
            plugin.reloadConfig();
            plugin.getSkillManager().loadData();
            sender.sendMessage("§a[NexoriaSkills] Skills data and configuration reloaded.");
            return true;
        }

        if (args.length >= 4 && args[0].equalsIgnoreCase("addxp")) {
            if (!sender.hasPermission("nexoria.skills.admin")) {
                sender.sendMessage("§cYou lack permission: nexoria.skills.admin");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + args[1]);
                return true;
            }
            SkillType st = SkillType.fromString(args[2]);
            if (st == null) {
                sender.sendMessage("§cInvalid skill: " + args[2]);
                return true;
            }
            try {
                double xp = Double.parseDouble(args[3]);
                plugin.getSkillManager().awardExp(target, st, xp);
                sender.sendMessage("§aAwarded §e" + xp + " XP §ain §e" + st.getDisplayName() + " §ato §f" + target.getName() + "§a.");
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid number: " + args[3]);
            }
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§8[§6Skills§8] §7Running on " + Bukkit.getServer().getName());
            return true;
        }

        SkillProfile prof = plugin.getSkillManager().getProfile(player.getUniqueId());
        player.sendMessage("§8================ §6Your Skill Attributes §8================");
        for (SkillType s : SkillType.values()) {
            int lvl = prof.getLevel(s);
            double xp = prof.getExp(s);
            double req = prof.getRequiredExp(s);
            int percent = (int) Math.min(100.0, (xp / req) * 100.0);
            player.sendMessage("§6• " + s.getDisplayName() + " §8- §eLevel " + lvl + " §8(§7" + String.format("%.0f", xp) + "/" + String.format("%.0f", req) + " XP §8- §a" + percent + "%§8)");
        }
        player.sendMessage("§8========================================================");
        return true;
    }

    private List<String> getSuggestions(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            List<String> list = new ArrayList<>();
            if (sender.hasPermission("nexoria.skills.admin")) {
                list.add("reload");
                list.add("addxp");
            }
            return list;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("addxp")) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                names.add(p.getName());
            }
            return names;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("addxp")) {
            List<String> skills = new ArrayList<>();
            for (SkillType s : SkillType.values()) {
                skills.add(s.name().toLowerCase());
            }
            return skills;
        }
        return List.of();
    }
}
