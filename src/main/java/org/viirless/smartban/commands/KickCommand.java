package org.viirless.smartban.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.viirless.smartban.BanPlugin;

public class KickCommand implements CommandExecutor {
    private final BanPlugin plugin;

    public KickCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banplugin.kick")) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.usage-kick")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.player-not-online")));
            return true;
        }

        // Prevent players from kicking themselves
        if (sender instanceof Player && target.getName().equals(((Player) sender).getName())) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.cannot-target-self")));
            return true;
        }

        // Staff-Bypass Check
        if (target.hasPermission("banplugin.bypass") || target.isOp()) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.staff-bypass")));
            return true;
        }

        // Combine remaining arguments for kick reason
        StringBuilder reason = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reason.append(args[i]);
            if (i < args.length - 1)
                reason.append(" ");
        }

        // Kick the player
        String kickMessage = plugin.getConfig().getString("messages.player-kicked")
                .replace("{reason}", reason.toString());
        target.kickPlayer(plugin.colorize(kickMessage));

        // Send confirmation message
        String successMessage = plugin.getConfig().getString("messages.kick-success")
                .replace("{player}", target.getName())
                .replace("{reason}", reason.toString());
        sender.sendMessage(plugin.colorize(successMessage));

        return true;
    }
}