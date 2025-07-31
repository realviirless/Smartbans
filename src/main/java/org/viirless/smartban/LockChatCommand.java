package org.viirless.smartban;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LockChatCommand implements CommandExecutor {
    private final BanPlugin plugin;
    private boolean chatLocked = false;

    public LockChatCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banplugin.lockchat")) {
            sender.sendMessage(plugin.getConfig().getString("messages.no-permission")
                    .replace("&", "§"));
            return true;
        }

        if (chatLocked) {
            sender.sendMessage(plugin.getConfig().getString("messages.chat.already-locked")
                    .replace("&", "§"));
            return true;
        }

        chatLocked = true;
        String lockMessage = plugin.getConfig().getString("messages.chat.locked")
                .replace("&", "§")
                .replace("{staff}", sender.getName());

        Bukkit.broadcastMessage(lockMessage);
        return true;
    }

    public boolean isChatLocked() {
        return chatLocked;
    }

    public void setChatLocked(boolean locked) {
        this.chatLocked = locked;
    }
}
