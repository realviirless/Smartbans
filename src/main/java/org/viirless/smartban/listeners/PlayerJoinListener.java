package org.viirless.smartban.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.viirless.smartban.BanPlugin;

import java.util.Date;

public class PlayerJoinListener implements Listener {

    private final BanPlugin plugin;

    public PlayerJoinListener(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if player is banned
        if (plugin.getBansConfig().contains("banned-players." + player.getUniqueId().toString())) {
            String uuid = event.getPlayer().getUniqueId().toString();
            String banPath = "banned-players." + uuid;

            // Get ban details
            long banTime = plugin.getBansConfig().getLong(banPath + ".time");
            long duration = plugin.getBansConfig().getLong(banPath + ".duration");
            String reason = plugin.getBansConfig().getString(banPath + ".reason");

            // Calculate expiry time (-1 duration means permanent ban)
            if (duration == -1) {
                // Permanent ban
                kickBannedPlayer(event, reason, -1);
                return;
            }

            long expiryTime = banTime + duration;

            // Check if ban has expired
            if (System.currentTimeMillis() >= expiryTime) {
                // Ban has expired, remove it
                plugin.getBansConfig().set(banPath, null);
                plugin.saveBansConfig();
                return; // Let player join
            }

            // Ban is still active, kick the player
            kickBannedPlayer(event, reason, expiryTime);
        }

        // Hide vanished players from the joining player
        if (!player.hasPermission("banplugin.vanish.see")) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (plugin.getVanishedPlayers().contains(onlinePlayer.getUniqueId())) {
                    player.hidePlayer(plugin, onlinePlayer);
                }
            }
        }

        // If the joining player is vanished, hide them from others
        if (plugin.getVanishedPlayers().contains(player.getUniqueId())) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.hasPermission("banplugin.vanish.see")) {
                    onlinePlayer.hidePlayer(plugin, player);
                }
            }
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.vanish.enabled")));
        }
    }

    private void kickBannedPlayer(PlayerJoinEvent event, String reason, long expiryTime) {
        String kickMessage = plugin.getConfig().getString("messages.player-banned")
                .replace("{reason}", reason)
                .replace("{expires}", expiryTime == -1 ? "Never" : new Date(expiryTime).toString());

        event.getPlayer().kickPlayer(colorize(kickMessage));
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}