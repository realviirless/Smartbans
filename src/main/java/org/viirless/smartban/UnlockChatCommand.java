package org.viirless.smartban;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UnlockChatCommand implements CommandExecutor {
    private final BanPlugin plugin;
    private final LockChatCommand lockChatCommand;

    public UnlockChatCommand(BanPlugin plugin, LockChatCommand lockChatCommand) {
        this.plugin = plugin;
        this.lockChatCommand = lockChatCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banplugin.lockchat")) {
            sender.sendMessage(plugin.getConfig().getString("messages.no-permission")
                    .replace("&", "§"));
            return true;
        }

        if (!lockChatCommand.isChatLocked()) {
            sender.sendMessage(plugin.getConfig().getString("messages.chat.already-unlocked")
                    .replace("&", "§"));
            return true;
        }

        lockChatCommand.setChatLocked(false);
        String unlockMessage = plugin.getConfig().getString("messages.chat.unlocked")
                .replace("&", "§")
                .replace("{staff}", sender.getName());

        Bukkit.broadcastMessage(unlockMessage);
        return true;
    }
}
