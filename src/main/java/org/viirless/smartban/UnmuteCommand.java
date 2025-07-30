package org.viirless.smartban;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

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
                plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this command!")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.usage-unmute", "&cUsage: /unmute <player>")));
            return true;
        }

        String playerName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        if (!plugin.getBansConfig().contains("muted-players." + target.getUniqueId().toString())) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.not-muted", "&cPlayer is not muted!")
                    .replace("{player}", target.getName())));
            return true;
        }

        // Remove mute from config
        plugin.getBansConfig().set("muted-players." + target.getUniqueId().toString(), null);
        plugin.saveBansConfig();

        // Send success messages
        String unmuteMessage = plugin.getConfig().getString("messages.unmute-success", "&aSuccessfully unmuted {player}")
            .replace("{player}", target.getName());
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', unmuteMessage));

        if (target.isOnline()) {
            String playerMessage = plugin.getConfig().getString("messages.player-unmuted", "&aYou have been unmuted!");
            target.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', playerMessage));
        }

        return true;
    }
}
