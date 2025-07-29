package org.viirless.smartban;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Date;
import java.util.Set;

public class BanCommand implements CommandExecutor {

    private final BanPlugin plugin;

    public BanCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("banplugin.ban")) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        // Check arguments
        if (args.length == 0) {
            // Show available ban IDs, reasons, and durations
            sender.sendMessage(colorize("&6Available Ban IDs:"));
            sender.sendMessage(colorize("&7Usage: &e/ban <player> <id>"));
            sender.sendMessage("");

            if (plugin.getConfig().getConfigurationSection("bans") != null) {
                for (String id : plugin.getConfig().getConfigurationSection("bans").getKeys(false)) {
                    String reason = plugin.getConfig().getString("bans." + id + ".reason");
                    String duration = plugin.getConfig().getString("bans." + id + ".duration");
                    sender.sendMessage(colorize("&eID " + id + ": &f" + reason + " &7(" + duration + ")"));
                }
            }
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.usage-ban")));
            return true;
        }

        String playerName = args[0];
        String banIdStr = args[1];

        // Validate ban ID
        int banId;
        try {
            banId = Integer.parseInt(banIdStr);
        } catch (NumberFormatException e) {
            Set<String> availableIds = plugin.getConfig().getConfigurationSection("bans").getKeys(false);
            String idsString = String.join(", ", availableIds);
            String message = plugin.getConfig().getString("messages.invalid-ban-id")
                    .replace("{ids}", idsString);
            sender.sendMessage(colorize(message));
            return true;
        }

        // Check if ban ID exists in config
        if (!plugin.getConfig().contains("bans." + banId)) {
            Set<String> availableIds = plugin.getConfig().getConfigurationSection("bans").getKeys(false);
            String idsString = String.join(", ", availableIds);
            String message = plugin.getConfig().getString("messages.invalid-ban-id")
                    .replace("{ids}", idsString);
            sender.sendMessage(colorize(message));
            return true;
        }

        // Get player (online or offline) - allows pre-emptive bans
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        // Check if target has bypass permission (prevent banning staff)
        if (target.isOnline()) {
            Player onlineTarget = (Player) target;
            if (onlineTarget.hasPermission("banplugin.bypass")) {
                sender.sendMessage(colorize(plugin.getConfig().getString("messages.cannot-ban-staff")));
                return true;
            }
        }

        // Check if player is already banned
        if (plugin.getBansConfig().contains("banned-players." + target.getUniqueId().toString())) {
            String message = plugin.getConfig().getString("messages.already-banned")
                    .replace("{player}", target.getName());
            sender.sendMessage(colorize(message));
            return true;
        }

        // Get ban details from config
        String reason = plugin.getConfig().getString("bans." + banId + ".reason");
        String duration = plugin.getConfig().getString("bans." + banId + ".duration");

        // Calculate expiry time
        long expiryTime = System.currentTimeMillis() + parseDuration(duration);

        // Store ban in bans.yml
        String uuid = target.getUniqueId().toString();
        plugin.getBansConfig().set("banned-players." + uuid + ".reason", reason);
        plugin.getBansConfig().set("banned-players." + uuid + ".expires", expiryTime);
        plugin.getBansConfig().set("banned-players." + uuid + ".banned-by", sender.getName());
        plugin.getBansConfig().set("banned-players." + uuid + ".banned-at", System.currentTimeMillis());
        plugin.saveBansConfig();

        // Kick player if online
        if (target.isOnline()) {
            Player onlineTarget = (Player) target;
            String kickMessage = plugin.getConfig().getString("messages.player-banned")
                    .replace("{reason}", reason)
                    .replace("{expires}", new Date(expiryTime).toString());
            onlineTarget.kickPlayer(colorize(kickMessage));
        }

        // Send success message
        String successMessage = plugin.getConfig().getString("messages.ban-success")
                .replace("{player}", target.getName())
                .replace("{reason}", reason)
                .replace("{duration}", duration);
        sender.sendMessage(colorize(successMessage));

        return true;
    }

    private long parseDuration(String duration) {
        if (duration == null || duration.isEmpty()) {
            return 24 * 60 * 60 * 1000; // Default 1 day
        }

        String timeUnit = duration.substring(duration.length() - 1).toLowerCase();
        String timeValue = duration.substring(0, duration.length() - 1);

        try {
            long value = Long.parseLong(timeValue);
            switch (timeUnit) {
                case "s":
                    return value * 1000;
                case "m":
                    return value * 60 * 1000;
                case "h":
                    return value * 60 * 60 * 1000;
                case "d":
                    return value * 24 * 60 * 60 * 1000;
                default:
                    return 24 * 60 * 60 * 1000; // Default 1 day
            }
        } catch (NumberFormatException e) {
            return 24 * 60 * 60 * 1000; // Default 1 day
        }
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}