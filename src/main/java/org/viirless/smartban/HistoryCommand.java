package org.viirless.smartban;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryCommand implements CommandExecutor {
    private final BanPlugin plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public HistoryCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dieser Befehl kann nur von Spielern ausgeführt werden!");
            return true;
        }

        if (!sender.hasPermission("banplugin.history")) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.history-usage")));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.invalid-player")));
            return true;
        }

        Player player = (Player) sender;
        openHistoryGUI(player, target);
        return true;
    }

    private void openHistoryGUI(Player viewer, OfflinePlayer target) {
        String title = colorize(plugin.getConfig().getString("messages.history.title")
                .replace("{player}", target.getName()));
        Inventory gui = Bukkit.createInventory(null, 54, title);

        ConfigurationSection history = plugin.getHistoryConfig().getConfigurationSection(target.getUniqueId().toString());
        if (history == null) {
            ItemStack noHistory = new ItemStack(Material.BARRIER);
            ItemMeta meta = noHistory.getItemMeta();
            meta.setDisplayName(colorize(plugin.getConfig().getString("messages.history.no-entries")));
            noHistory.setItemMeta(meta);
            gui.setItem(22, noHistory);
            viewer.openInventory(gui);
            return;
        }

        List<ItemStack> historyItems = new ArrayList<>();
        for (String key : history.getKeys(false)) {
            ConfigurationSection entry = history.getConfigurationSection(key);
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();

            String type = entry.getString("type");
            String by = entry.getString("by");
            String reason = entry.getString("reason");
            long date = entry.getLong("date");
            long duration = entry.getLong("duration");

            meta.setDisplayName(colorize(plugin.getConfig().getString("messages.history.entry." + type.toLowerCase())));

            List<String> lore = new ArrayList<>();
            lore.add(colorize(plugin.getConfig().getString("messages.history.entry.by")
                    .replace("{staff}", by)));
            lore.add(colorize(plugin.getConfig().getString("messages.history.entry.reason")
                    .replace("{reason}", reason)));
            lore.add(colorize(plugin.getConfig().getString("messages.history.entry.date")
                    .replace("{date}", dateFormat.format(new Date(date)))));

            if (duration == -1) {
                lore.add(colorize(plugin.getConfig().getString("messages.history.entry.duration.permanent")));
            } else {
                lore.add(colorize(plugin.getConfig().getString("messages.history.entry.duration.temporary")
                        .replace("{duration}", formatDuration(duration))));
            }

            meta.setLore(lore);
            paper.setItemMeta(meta);
            historyItems.add(paper);
        }

        for (int i = 0; i < Math.min(historyItems.size(), 54); i++) {
            gui.setItem(i, historyItems.get(i));
        }

        viewer.openInventory(gui);
    }

    private String formatDuration(long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day(s)";
        } else if (hours > 0) {
            return hours + " hour(s)";
        } else if (minutes > 0) {
            return minutes + " minute(s)";
        } else {
            return seconds + " second(s)";
        }
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
