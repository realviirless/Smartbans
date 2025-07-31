package org.viirless.smartban;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final BanPlugin plugin;
    private final LockChatCommand lockChatCommand;

    public ChatListener(BanPlugin plugin, LockChatCommand lockChatCommand) {
        this.plugin = plugin;
        this.lockChatCommand = lockChatCommand;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Check if chat is locked
        if (lockChatCommand.isChatLocked() && !player.hasPermission("banplugin.lockchat.bypass")) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.chat.no-permission-write")));
            return;
        }

        // Check mute status
        String uuid = player.getUniqueId().toString();
        String path = "muted-players." + uuid;

        // Debug output
        plugin.getLogger().info("Checking mute status for " + player.getName());
        plugin.getLogger().info("Checking path: " + path);
        plugin.getLogger().info("Contains path: " + plugin.getBansConfig().contains(path));

        if (plugin.getBansConfig().contains(path)) {
            long expireTime = plugin.getBansConfig().getLong(path + ".expires");
            plugin.getLogger().info("Expire time: " + expireTime + ", Current time: " + System.currentTimeMillis());

            if (System.currentTimeMillis() >= expireTime) {
                plugin.getLogger().info("Mute expired, removing...");
                plugin.getBansConfig().set(path, null);
                plugin.saveBansConfig();
                return;
            }

            String reason = plugin.getBansConfig().getString(path + ".reason");
            long remainingTime = expireTime - System.currentTimeMillis();
            String duration = formatDuration(remainingTime);

            event.setCancelled(true);

            String muteMessage = plugin.getConfig().getString("messages.chat-while-muted",
                "&cYou are muted for {duration}! Reason: {reason}")
                .replace("{duration}", duration)
                .replace("{reason}", reason);

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', muteMessage));
            plugin.getLogger().info("Blocked chat message from muted player: " + player.getName());
        }
    }

    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + hours % 24 + "h";
        } else if (hours > 0) {
            return hours + "h " + minutes % 60 + "m";
        } else if (minutes > 0) {
            return minutes + "m " + seconds % 60 + "s";
        } else {
            return seconds + "s";
        }
    }
}
