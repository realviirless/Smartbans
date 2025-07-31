package org.viirless.smartban;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class VanishCommand implements CommandExecutor {
    private final BanPlugin plugin;

    public VanishCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.colorize("&cThis command can only be used by players!"));
            return true;
        }

        if (!sender.hasPermission("banplugin.vanish")) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getVanishedPlayers().contains(player.getUniqueId())) {
            // Unvanish the player
            plugin.getVanishedPlayers().remove(player.getUniqueId());

            // Show player to everyone
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.showPlayer(plugin, player);
            }

            player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.vanish.disabled")));
        } else {
            // Vanish the player
            plugin.getVanishedPlayers().add(player.getUniqueId());

            // Hide player from everyone except those with see permission
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.hasPermission("banplugin.vanish.see")) {
                    onlinePlayer.hidePlayer(plugin, player);
                }
            }

            player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.vanish.enabled")));
        }

        return true;
    }
}
