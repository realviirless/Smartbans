package org.viirless.smartban;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class KickCommand implements CommandExecutor {

    private final BanPlugin plugin;

    public KickCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Skip permission check if sender is console
        if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission("banplugin.kick")) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        // Show usage if no arguments
        if (args.length == 0) {
            showKickUsage(sender);
            return true;
        }

        // Need at least player and one word for reason
        if (args.length < 2) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.usage-kick")));
            return true;
        }

        String playerName = args[0];
        Player target = Bukkit.getPlayer(playerName);

        // Check if player is online
        if (target == null) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.player-not-online")));
            return true;
        }

        // Check bypass permission (skip for console)
        if (!(sender instanceof ConsoleCommandSender) && target.hasPermission("banplugin.bypass")) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.cannot-kick-staff")));
            return true;
        }

        // Combine all arguments after player name for reason
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]);
            if (i < args.length - 1) {
                reasonBuilder.append(" ");
            }
        }
        String reason = reasonBuilder.toString();

        // Kick player
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

    private void showKickUsage(CommandSender sender) {
        String divider = plugin.getConfig().getString("usage-format.divider");
        String header = plugin.getConfig().getString("usage-format.kick-command.header");
        String listHeader = plugin.getConfig().getString("usage-format.kick-command.list-header");
        String info = plugin.getConfig().getString("usage-format.kick-command.info");
        String example = plugin.getConfig().getString("usage-format.kick-command.example");

        sender.sendMessage(colorize(divider));
        sender.sendMessage(colorize(header));
        sender.sendMessage(colorize(divider));
        sender.sendMessage(colorize(listHeader));
        sender.sendMessage(colorize(info));
        sender.sendMessage(colorize(example));
        sender.sendMessage(colorize(divider));
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}