package org.viirless.smartban.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.viirless.smartban.BanPlugin;

public class ClearInventoryCommand implements CommandExecutor {
    private final BanPlugin plugin;

    public ClearInventoryCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banplugin.clearinv")) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        // If no args, clear own inventory (if sender is player)
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.clearinv.usage")));
                return true;
            }
            Player player = (Player) sender;
            clearInventory(player);
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.clearinv.cleared-self")));
            return true;
        }

        // Clear target player's inventory
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.player-not-online")));
            return true;
        }

        // Prevent clearing inventory of staff members
        if (target.hasPermission("banplugin.clearinv.bypass")) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.staff-bypass")));
            return true;
        }

        clearInventory(target);
        sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.clearinv.cleared-other")
                .replace("{player}", target.getName())));
        target.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.clearinv.cleared-by")
                .replace("{staff}", sender.getName())));

        return true;
    }

    private void clearInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);
    }
}
