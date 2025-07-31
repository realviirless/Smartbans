package org.viirless.smartban;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class InvseeCommand implements CommandExecutor, Listener {
    private final BanPlugin plugin;

    public InvseeCommand(BanPlugin plugin) {
        this.plugin = plugin;
        // Register this class as event listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.colorize("&cThis command can only be used by players!"));
            return true;
        }

        if (!sender.hasPermission("banplugin.invsee")) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.invsee.usage")));
            return true;
        }

        Player staff = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.player-not-online")));
            return true;
        }

        // Prevent players from viewing their own inventory
        if (target.equals(staff)) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.cannot-target-self")));
            return true;
        }

        // Prevent viewing inventory of staff members
        if (target.hasPermission("banplugin.invsee.bypass")) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.staff-bypass")));
            return true;
        }

        // Open inventory and send message
        Inventory inv = target.getInventory();
        staff.openInventory(inv);

        if (!staff.hasPermission("banplugin.invsee.modify")) {
            staff.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.invsee.view-only")));
        }

        staff.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.invsee.opened")
                .replace("{player}", target.getName())));

        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        // Check if this is an invsee inventory
        if (event.getInventory().equals(event.getWhoClicked().getOpenInventory().getTopInventory()) &&
                event.getInventory().getHolder() instanceof Player &&
                !event.getWhoClicked().equals(event.getInventory().getHolder())) {

            // Cancel if player doesn't have modify permission
            if (!event.getWhoClicked().hasPermission("banplugin.invsee.modify")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        // Check if this is an invsee inventory
        if (event.getInventory().equals(event.getWhoClicked().getOpenInventory().getTopInventory()) &&
                event.getInventory().getHolder() instanceof Player &&
                !event.getWhoClicked().equals(event.getInventory().getHolder())) {

            // Cancel if player doesn't have modify permission
            if (!event.getWhoClicked().hasPermission("banplugin.invsee.modify")) {
                event.setCancelled(true);
            }
        }
    }
}
