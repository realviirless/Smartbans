package org.viirless.smartban.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.viirless.smartban.BanPlugin;

public class ReloadCommand implements CommandExecutor {
    private final BanPlugin plugin;

    public ReloadCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banplugin.reload")) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        // Reload all configurations
        plugin.reloadConfig();
        plugin.reloadBansConfig();
        plugin.reloadHistoryConfig();

        sender.sendMessage(colorize(plugin.getConfig().getString("messages.reload-success")));
        return true;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
