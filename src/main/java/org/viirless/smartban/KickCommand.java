package org.viirless.smartban;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.Set;

public class KickCommand implements CommandExecutor {

    private final BanPlugin plugin;

    public KickCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("banplugin.kick")) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        // Check arguments
        if (args.length < 2) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.usage-kick")));
            return true;
        }

        String playerName = args[0];

        // Join all arguments after player name as the reason
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]);
            if (i < args.length - 1) {
                reasonBuilder.append(" ");
            }
        }
        String reason = reasonBuilder.toString();

        // Get online player
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.player-not-online")));
            return true;
        }

        // Check if target has kick bypass permission
        if (target.hasPermission("banplugin.kick.bypass")) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.cannot-kick-staff")));
            return true;
        }

        // Kick the player
        String kickMessage = plugin.getConfig().getString("messages.player-kicked")
                .replace("{reason}", reason);
        target.kickPlayer(colorize(kickMessage));

        // Send success message
        String successMessage = plugin.getConfig().getString("messages.kick-success")
                .replace("{player}", target.getName())
                .replace("{reason}", reason);
        sender.sendMessage(colorize(successMessage));

        return true;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}