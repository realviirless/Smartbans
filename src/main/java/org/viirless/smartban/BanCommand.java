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
import org.bukkit.configuration.ConfigurationSection;

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
            showBanUsage(sender);
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.usage-ban")));
            return true;
        }

        String playerName = args[0];
        boolean useIdSystem = plugin.getConfig().getBoolean("settings.use-id-system.ban", true);

        // Combine all arguments after player name for the reason/id
        StringBuilder inputBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            inputBuilder.append(args[i]);
            if (i < args.length - 1) {
                inputBuilder.append(" ");
            }
        }
        String input = inputBuilder.toString();

        // Find the ban entry either by ID or reason
        String banId = null;
        ConfigurationSection bans = plugin.getConfig().getConfigurationSection("bans");

        if (useIdSystem) {
            // Using ID system
            if (!bans.contains(input)) {
                Set<String> availableIds = bans.getKeys(false);
                String idsString = String.join(", ", availableIds);
                String message = plugin.getConfig().getString("messages.invalid-ban-id")
                        .replace("{ids}", idsString);
                sender.sendMessage(colorize(message));
                return true;
            }
            banId = input;
        } else {
            // Using reason system - find the ID by reason
            for (String id : bans.getKeys(false)) {
                String reason = bans.getString(id + ".reason");
                if (reason != null && reason.equalsIgnoreCase(input)) {
                    banId = id;
                    break;
                }
            }

            if (banId == null) {
                sender.sendMessage(colorize("&cInvalid ban reason! Use TAB to see available reasons."));
                return true;
            }
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

    private void showBanUsage(CommandSender sender) {
        String divider = plugin.getConfig().getString("usage-format.divider", "&7&m--------------------------------");
        boolean useIdSystem = plugin.getConfig().getBoolean("settings.use-id-system.ban", true);

        String header = useIdSystem ?
            plugin.getConfig().getString("usage-format.ban-command.header", "&cUsage: &7/ban <player> <ban-id>") :
            "&cUsage: &7/ban <player> <reason>";

        String listHeader = useIdSystem ?
            plugin.getConfig().getString("usage-format.ban-command.list-header", "&cAvailable Ban IDs:") :
            "&cAvailable Ban Reasons:";

        String format = useIdSystem ?
            plugin.getConfig().getString("usage-format.ban-command.format", "&7ID: &c{id} &7| Reason: &c{reason} &7| Duration: &c{duration}") :
            "&7Reason: &c{reason} &7| Duration: &c{duration}";

        sender.sendMessage(colorize(divider));
        sender.sendMessage(colorize(header));
        sender.sendMessage(colorize(divider));
        sender.sendMessage(colorize(listHeader));

        if (plugin.getConfig().getConfigurationSection("bans") != null) {
            for (String id : plugin.getConfig().getConfigurationSection("bans").getKeys(false)) {
                String reason = plugin.getConfig().getString("bans." + id + ".reason");
                String duration = plugin.getConfig().getString("bans." + id + ".duration");
                String line = format
                    .replace("{id}", id)
                    .replace("{reason}", reason)
                    .replace("{duration}", duration);
                sender.sendMessage(colorize(line));
            }
        }

        sender.sendMessage(colorize(divider));
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