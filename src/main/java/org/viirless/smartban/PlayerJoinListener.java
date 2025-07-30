package org.viirless.smartban;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.Date;

public class PlayerJoinListener implements Listener {

    private final BanPlugin plugin;

    public PlayerJoinListener(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        String banPath = "banned-players." + uuid;

        // Check if player is banned
        if (!plugin.getBansConfig().contains(banPath)) {
            return; // Player is not banned
        }

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