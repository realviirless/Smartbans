package org.viirless.smartban.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.viirless.smartban.BanPlugin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HistoryCommand implements CommandExecutor {
    private final BanPlugin plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public HistoryCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banplugin.history")) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.colorize("&cThis command can only be used by players!"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.history-usage")));
            return true;
        }

        Player player = (Player) sender;
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        String uuid = target != null ? target.getUniqueId().toString()
                : Bukkit.getOfflinePlayer(targetName).getUniqueId().toString();

        ConfigurationSection history = plugin.getHistoryConfig().getConfigurationSection(uuid);
        if (history == null || history.getKeys(false).isEmpty()) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.history.no-entries")));
            return true;
        }

        openHistoryGUI(player, targetName, uuid);
        return true;
    }

    private void openHistoryGUI(Player player, String targetName, String uuid) {
        Inventory gui = Bukkit.createInventory(null, 54, plugin.colorize(
                plugin.getConfig().getString("messages.history.title").replace("{player}", targetName)));

        ConfigurationSection history = plugin.getHistoryConfig().getConfigurationSection(uuid);
        if (history == null)
            return;

        int slot = 0;
        for (String timestamp : history.getKeys(false)) {
            if (slot >= 54)
                break;

            ConfigurationSection entry = history.getConfigurationSection(timestamp);
            if (entry == null)
                continue;

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta == null)
                continue;

            String type = entry.getString("type", "UNKNOWN");
            meta.setDisplayName(
                    plugin.colorize(plugin.getConfig().getString("messages.history.entry." + type.toLowerCase())));

            List<String> lore = new ArrayList<>();
            lore.add(plugin.colorize(plugin.getConfig().getString("messages.history.entry.by")
                    .replace("{staff}", entry.getString("by", "Unknown"))));
            lore.add(plugin.colorize(plugin.getConfig().getString("messages.history.entry.reason")
                    .replace("{reason}", entry.getString("reason", "Unknown"))));
            lore.add(plugin.colorize(plugin.getConfig().getString("messages.history.entry.date")
                    .replace("{date}", dateFormat.format(new Date(entry.getLong("date"))))));

            // Add status and remaining time
            String status = entry.getString("status", "Unknown");
            if (status.equals("Active")) {
                long expiryTime = entry.getLong("expiry", 0);
                if (expiryTime > System.currentTimeMillis()) {
                    long remaining = expiryTime - System.currentTimeMillis();
                    lore.add(plugin.colorize("&7Status: &eActive &7(&e" + formatTime(remaining) + " remaining&7)"));
                } else {
                    // Automatically update to Finished if expired
                    plugin.updatePunishmentStatus(uuid, entry.getLong("date"), true);
                    lore.add(plugin.colorize("&7Status: &aFinished"));
                }
            } else {
                lore.add(plugin.colorize("&7Status: &" + (status.equals("Permanent") ? "c" : "a") + status));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }

        player.openInventory(gui);
    }

    private String formatTime(long millis) {
        if (millis < 0)
            return "0 seconds";

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder();
        if (days > 0)
            sb.append(days).append("d ");
        if (hours > 0)
            sb.append(hours).append("h ");
        if (minutes > 0)
            sb.append(minutes).append("m ");
        if (seconds > 0)
            sb.append(seconds).append("s");

        return sb.toString().trim();
    }
}
