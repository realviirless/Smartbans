package org.viirless.smartban;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class MuteCommand implements CommandExecutor {
    private final BanPlugin plugin;

    public MuteCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Skip permission check if sender is console
        if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission("banplugin.mute")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this command!")));
            return true;
        }

        if (args.length == 0) {
            showMuteUsage(sender);
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.usage-mute")));
            return true;
        }

        String playerName = args[0];
        boolean useIdSystem = plugin.getConfig().getBoolean("settings.use-id-system.mute", true);

        // Combine all arguments after player name for the reason/id
        StringBuilder inputBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            inputBuilder.append(args[i]);
            if (i < args.length - 1) {
                inputBuilder.append(" ");
            }
        }
        String input = inputBuilder.toString();

        // Find the mute entry either by ID or reason
        String muteId = null;
        ConfigurationSection mutes = plugin.getConfig().getConfigurationSection("mutes");

        if (useIdSystem) {
            // Using ID system
            if (!mutes.contains(input)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.invalid-mute-id")));
                return true;
            }
            muteId = input;
        } else {
            // Using reason system - find the ID by reason
            for (String id : mutes.getKeys(false)) {
                String reason = mutes.getString(id + ".reason");
                if (reason != null && reason.equalsIgnoreCase(input)) {
                    muteId = id;
                    break;
                }
            }

            if (muteId == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&cInvalid mute reason! Use TAB to see available reasons."));
                return true;
            }
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        // Only check for bypass permission if target is online and sender is not console
        if (target.isOnline() && !(sender instanceof ConsoleCommandSender)) {
            Player onlineTarget = (Player) target;
            if (onlineTarget.hasPermission("banplugin.bypass")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.cannot-mute-staff")));
                return true;
            }
        }

        if (plugin.getBansConfig().contains("muted-players." + target.getUniqueId().toString())) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.already-muted").replace("{player}", target.getName())));
            return true;
        }

        String reason = plugin.getConfig().getString("mutes." + muteId + ".reason");
        String duration = plugin.getConfig().getString("mutes." + muteId + ".duration");
        long expiryTime = System.currentTimeMillis() + parseDuration(duration);

        String uuid = target.getUniqueId().toString();
        plugin.getBansConfig().set("muted-players." + uuid + ".reason", reason);
        plugin.getBansConfig().set("muted-players." + uuid + ".expires", expiryTime);
        plugin.getBansConfig().set("muted-players." + uuid + ".muted-by", sender instanceof ConsoleCommandSender ? "CONSOLE" : sender.getName());
        plugin.getBansConfig().set("muted-players." + uuid + ".muted-at", System.currentTimeMillis());

        // Debug output
        plugin.getLogger().info("Muting player " + target.getName() + " (UUID: " + uuid + ")");
        plugin.getLogger().info("Mute data: reason=" + reason + ", expires=" + expiryTime);

        plugin.saveBansConfig();
        // Reload the config to ensure it's saved
        plugin.reloadBansConfig();

        String muteMessage = plugin.getConfig().getString("messages.mute-success")
            .replace("{player}", target.getName())
            .replace("{reason}", reason)
            .replace("{duration}", duration);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', muteMessage));

        if (target.isOnline()) {
            String playerMessage = plugin.getConfig().getString("messages.player-muted")
                .replace("{reason}", reason)
                .replace("{expires}", duration);
            target.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', playerMessage));
        }

        return true;
    }

    private void showMuteUsage(CommandSender sender) {
        String divider = plugin.getConfig().getString("usage-format.divider", "&7&m--------------------------------");
        boolean useIdSystem = plugin.getConfig().getBoolean("settings.use-id-system.mute", true);

        String header = useIdSystem ?
            plugin.getConfig().getString("usage-format.mute-command.header", "&cUsage: &7/mute <player> <mute-id>") :
            "&cUsage: &7/mute <player> <reason>";

        String listHeader = useIdSystem ?
            plugin.getConfig().getString("usage-format.mute-command.list-header", "&cAvailable Mute IDs:") :
            "&cAvailable Mute Reasons:";

        String format = useIdSystem ?
            plugin.getConfig().getString("usage-format.mute-command.format", "&7ID: &c{id} &7| Reason: &c{reason} &7| Duration: &c{duration}") :
            "&7Reason: &c{reason} &7| Duration: &c{duration}";

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', divider));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', divider));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', listHeader));

        ConfigurationSection mutes = plugin.getConfig().getConfigurationSection("mutes");
        if (mutes != null) {
            for (String id : mutes.getKeys(false)) {
                String reason = mutes.getString(id + ".reason");
                String duration = mutes.getString(id + ".duration");
                String line = format
                    .replace("{id}", id)
                    .replace("{reason}", reason)
                    .replace("{duration}", duration);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
            }
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', divider));
    }

    private long parseDuration(String duration) {
        if (duration == null) return 0;

        long multiplier;
        if (duration.endsWith("s")) {
            multiplier = 1000; // Sekunden zu Millisekunden
        } else if (duration.endsWith("m")) {
            multiplier = 1000 * 60; // Minuten zu Millisekunden
        } else if (duration.endsWith("h")) {
            multiplier = 1000 * 60 * 60; // Stunden zu Millisekunden
        } else if (duration.endsWith("d")) {
            multiplier = 1000 * 60 * 60 * 24; // Tage zu Millisekunden
        } else {
            return 0;
        }

        try {
            String numberPart = duration.substring(0, duration.length() - 1);
            return Long.parseLong(numberPart) * multiplier;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
