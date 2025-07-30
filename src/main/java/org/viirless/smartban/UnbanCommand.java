package org.viirless.smartban;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UnbanCommand implements CommandExecutor {

    private final BanPlugin plugin;

    public UnbanCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("banplugin.unban")) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        // Check arguments
        if (args.length != 1) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.usage-unban")));
            return true;
        }

        String playerName = args[0];

        // Get player (online or offline) - allows unbanning pre-emptive bans
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        String uuid = target.getUniqueId().toString();

        // Check if player is banned
        if (!plugin.getBansConfig().contains("banned-players." + uuid)) {
            String message = plugin.getConfig().getString("messages.not-banned")
                    .replace("{player}", target.getName());
            sender.sendMessage(colorize(message));
            return true;
        }

        // Remove ban from bans.yml
        plugin.getBansConfig().set("banned-players." + uuid, null);
        plugin.saveBansConfig();

        // Send success message
        String successMessage = plugin.getConfig().getString("messages.unban-success")
                .replace("{player}", target.getName());
        sender.sendMessage(colorize(successMessage));

        return true;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}