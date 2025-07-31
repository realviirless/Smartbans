package org.viirless.smartban;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ExamineCommand implements CommandExecutor {
    private final BanPlugin plugin;

    public ExamineCommand(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.colorize("&cThis command can only be used by players!"));
            return true;
        }

        if (!sender.hasPermission("banplugin.examine")) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.examine.usage")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.examine.offline")));
            return true;
        }

        openExamineGUI((Player) sender, target);
        return true;
    }

    private void openExamineGUI(Player staff, Player target) {
        Inventory gui = Bukkit.createInventory(null, 27, plugin.colorize(
                plugin.getConfig().getString("messages.examine.title")
                        .replace("{player}", target.getName())
        ));

        // Player head in the middle
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        skullMeta.setOwningPlayer(target);
        skullMeta.setDisplayName(plugin.colorize("&e" + target.getName()));
        List<String> headLore = new ArrayList<>();
        headLore.add(plugin.colorize(plugin.getConfig().getString("messages.examine.info.name")
                .replace("{name}", target.getName())));
        headLore.add(plugin.colorize(plugin.getConfig().getString("messages.examine.info.uuid")
                .replace("{uuid}", target.getUniqueId().toString())));
        skullMeta.setLore(headLore);
        head.setItemMeta(skullMeta);
        gui.setItem(13, head);

        // Player info (Paper with details)
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(plugin.colorize("&ePlayer Information"));
        List<String> infoLore = new ArrayList<>();
        infoLore.add(plugin.colorize(plugin.getConfig().getString("messages.examine.info.ip")
                .replace("{ip}", target.getAddress().getAddress().getHostAddress())));
        infoLore.add(plugin.colorize(plugin.getConfig().getString("messages.examine.info.location")
                .replace("{world}", target.getWorld().getName())
                .replace("{x}", String.format("%.2f", target.getLocation().getX()))
                .replace("{y}", String.format("%.2f", target.getLocation().getY()))
                .replace("{z}", String.format("%.2f", target.getLocation().getZ()))));
        infoLore.add(plugin.colorize(plugin.getConfig().getString("messages.examine.info.gamemode")
                .replace("{gamemode}", target.getGameMode().toString())));
        infoMeta.setLore(infoLore);
        info.setItemMeta(infoMeta);
        gui.setItem(11, info);

        // Status info (Compass)
        ItemStack status = new ItemStack(Material.COMPASS);
        ItemMeta statusMeta = status.getItemMeta();
        statusMeta.setDisplayName(plugin.colorize("&ePlayer Status"));
        List<String> statusLore = new ArrayList<>();
        statusLore.add(plugin.colorize(plugin.getConfig().getString("messages.examine.info.health")
                .replace("{health}", String.format("%.1f", target.getHealth()))));
        statusLore.add(plugin.colorize(plugin.getConfig().getString("messages.examine.info.food")
                .replace("{food}", String.valueOf(target.getFoodLevel()))));
        statusLore.add(plugin.colorize(plugin.getConfig().getString("messages.examine.info.exp")
                .replace("{exp}", String.valueOf(target.getLevel()))));
        statusLore.add(plugin.colorize(plugin.getConfig().getString("messages.examine.info.op")
                .replace("{op}", target.isOp() ? "Yes" : "No")));
        statusMeta.setLore(statusLore);
        status.setItemMeta(statusMeta);
        gui.setItem(15, status);

        // History button (Redstone)
        ItemStack history = new ItemStack(Material.REDSTONE);
        ItemMeta historyMeta = history.getItemMeta();
        historyMeta.setDisplayName(plugin.colorize("&cView History"));
        List<String> historyLore = new ArrayList<>();
        historyLore.add(plugin.colorize("&7Click to view"));
        historyLore.add(plugin.colorize("&7punishment history"));
        historyMeta.setLore(historyLore);
        history.setItemMeta(historyMeta);
        gui.setItem(26, history);

        staff.openInventory(gui);
    }
}
