package org.viirless.smartban.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.viirless.smartban.BanPlugin;

public class UnmuteCommand implements CommandExecutor {

    private final BanPlugin plugin;

    public UnmuteCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Skip permission check if sender is console
        if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission("banplugin.unmute")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission",
                            "&cYou don't have permission to use this command!")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.usage-unmute", "&cUsage: /unmute <player>")));
            return true;
        }

        String playerName = args[0];
        String uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId().toString();

        if (!plugin.getBansConfig().contains("muted-players." + uuid)) {
            String message = plugin.getConfig().getString("messages.not-muted")
                    .replace("{player}", playerName);
            sender.sendMessage(plugin.colorize(message));
            return true;
        }

        // Find and update the status of the active mute in history
        ConfigurationSection history = plugin.getHistoryConfig().getConfigurationSection(uuid);
        if (history != null) {
            for (String timestamp : history.getKeys(false)) {
                ConfigurationSection entry = history.getConfigurationSection(timestamp);
                if (entry != null && entry.getString("type").equals("MUTE")
                        && entry.getString("status", "").equals("Active")) {
                    plugin.updatePunishmentStatus(uuid, Long.parseLong(timestamp), true);
                    break;
                }
            }
        }

        // Remove mute from config
        plugin.getBansConfig().set("muted-players." + uuid, null);
        plugin.saveBansConfig();

        // Send success messages
        String unmuteMessage = plugin.getConfig()
                .getString("messages.unmute-success", "&aSuccessfully unmuted {player}")
                .replace("{player}", playerName);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', unmuteMessage));

        Player target = Bukkit.getPlayer(playerName);
        if (target != null) {
            String playerMessage = plugin.getConfig().getString("messages.player-unmuted", "&aYou have been unmuted!");
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', playerMessage));
        }

        return true;
    }
}
