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

        // Check if player is banned
        if (!plugin.getBansConfig().contains("banned-players." + uuid)) {
            return; // Player is not banned
        }

        // Get ban details
        long expiryTime = plugin.getBansConfig().getLong("banned-players." + uuid + ".expires");
        String reason = plugin.getBansConfig().getString("banned-players." + uuid + ".reason");

        // Check if ban has expired
        if (System.currentTimeMillis() >= expiryTime) {
            // Ban has expired, remove it
            plugin.getBansConfig().set("banned-players." + uuid, null);
            plugin.saveBansConfig();
            return; // Let player join
        }

        // Ban is still active, kick the player
        String kickMessage = plugin.getConfig().getString("messages.player-banned")
                .replace("{reason}", reason)
                .replace("{expires}", new Date(expiryTime).toString());

        event.getPlayer().kickPlayer(colorize(kickMessage));
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}