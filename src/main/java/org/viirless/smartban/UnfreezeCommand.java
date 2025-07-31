package org.viirless.smartban;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnfreezeCommand implements CommandExecutor {
    private final BanPlugin plugin;

    public UnfreezeCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banplugin.unfreeze")) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.unfreeze-usage")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.player-not-online")));
            return true;
        }

        if (!plugin.getFrozenPlayers().contains(target.getUniqueId())) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.freeze.not-frozen")));
            return true;
        }

        plugin.getFrozenPlayers().remove(target.getUniqueId());
        String message = plugin.getConfig().getString("messages.freeze.unfrozen-by")
                .replace("{player}", target.getName());
        sender.sendMessage(plugin.colorize(message));

        // Clear the freeze title
        target.resetTitle();
        target.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.freeze.unfrozen")));

        return true;
    }
}
