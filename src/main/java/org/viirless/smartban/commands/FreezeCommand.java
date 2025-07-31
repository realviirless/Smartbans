package org.viirless.smartban.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.viirless.smartban.BanPlugin;

public class FreezeCommand implements CommandExecutor {
    private final BanPlugin plugin;

    public FreezeCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banplugin.freeze")) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.freeze-usage")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.player-not-online")));
            return true;
        }

        // Prevent players from freezing themselves
        if (sender instanceof Player && target.getName().equals(((Player) sender).getName())) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.cannot-target-self")));
            return true;
        }

        if (plugin.getFrozenPlayers().contains(target.getUniqueId())) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.freeze.already-frozen")));
            return true;
        }

        // Check if the target player is OP or has the bypass permission
        if (target.isOp() || target.hasPermission("banplugin.freeze.bypass")) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.staff-bypass")));
            return true;
        }

        plugin.getFrozenPlayers().add(target.getUniqueId());
        String message = plugin.getConfig().getString("messages.freeze.frozen-by")
                .replace("{player}", target.getName());
        sender.sendMessage(plugin.colorize(message));

        String mainTitle = plugin.getConfig().getString("messages.freeze.title.main");
        String subtitle = plugin.getConfig().getString("messages.freeze.title.subtitle");

        target.sendTitle(
                plugin.colorize(mainTitle),
                plugin.colorize(subtitle),
                10,
                Integer.MAX_VALUE,
                20);

        return true;
    }
}
