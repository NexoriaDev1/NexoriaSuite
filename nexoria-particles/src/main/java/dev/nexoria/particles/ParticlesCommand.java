package dev.nexoria.particles;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ParticlesCommand implements BasicCommand, CommandExecutor, TabCompleter {

    private final NexoriaParticlesPlugin plugin;

    public ParticlesCommand(NexoriaParticlesPlugin plugin) {
        this.plugin = plugin;
    }

    private String msg(String key) {
        String p = plugin.getConfig().getString("messages.prefix", "&8[&dParticles&8] ");
        String m = plugin.getConfig().getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', p + m);
    }

    private void handleCommand(CommandSender sender, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("nexoria.particles.admin")) {
                sender.sendMessage(msg("no-permission"));
                return;
            }
            plugin.reloadConfig();
            plugin.getParticleManager().startTask();
            sender.sendMessage(msg("reloaded"));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only in-game players can equip cosmetic particles.");
            return;
        }

        if (!player.hasPermission("nexoria.particles.use")) {
            player.sendMessage(msg("no-permission"));
            return;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&dNexoriaParticles &7v" + plugin.getPluginMeta().getVersion()));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/particles halo  &7- Ring above your head"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/particles wings &7- Angel wings behind you"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/particles orbit &7- Orbiting rotating stars"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/particles trail &7- Footstep trailing leaves"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', " &e/particles clear &7- Clear active particles"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
            return;
        }

        String effect = args[0].toLowerCase();
        if (effect.equals("clear")) {
            plugin.getParticleManager().clear(player.getUniqueId());
            player.sendMessage(msg("cleared"));
            return;
        }

        if (effect.equals("halo") || effect.equals("wings") || effect.equals("orbit") || effect.equals("trail")) {
            plugin.getParticleManager().setEffect(player.getUniqueId(), effect);
            player.sendMessage(msg("applied").replace("{effect}", effect));
            return;
        }

        player.sendMessage(msg("invalid-effect"));
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        handleCommand(source.getSender(), args);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return getCompletions(source.getSender(), args);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        handleCommand(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return getCompletions(sender, args);
    }

    private List<String> getCompletions(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("halo");
            list.add("wings");
            list.add("orbit");
            list.add("trail");
            list.add("clear");
            if (sender.hasPermission("nexoria.particles.admin")) {
                list.add("reload");
            }
        }
        return list;
    }
}
