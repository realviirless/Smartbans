package org.viirless.smartban.commands;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.viirless.smartban.BanPlugin;

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

            // Stop actionbar message
            if (plugin.getActionBarTasks().containsKey(player.getUniqueId())) {
                plugin.getActionBarTasks().get(player.getUniqueId()).cancel();
                plugin.getActionBarTasks().remove(player.getUniqueId());
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

            // Start actionbar message
            startActionBarTask(player);

            player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.vanish.enabled")));
        }

        return true;
    }

    private void startActionBarTask(Player player) {
        // Cancel existing task if there is one
        if (plugin.getActionBarTasks().containsKey(player.getUniqueId())) {
            plugin.getActionBarTasks().get(player.getUniqueId()).cancel();
        }

        // Create new task
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !plugin.getVanishedPlayers().contains(player.getUniqueId())) {
                    this.cancel();
                    plugin.getActionBarTasks().remove(player.getUniqueId());
                    return;
                }

                String message = plugin.colorize(plugin.getConfig().getString("messages.vanish.actionbar"));
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
            }
        };

        // Run task every second (20 ticks) and store it as BukkitTask
        BukkitTask task = runnable.runTaskTimer(plugin, 0L, 20L);
        plugin.getActionBarTasks().put(player.getUniqueId(), task);
    }
}
