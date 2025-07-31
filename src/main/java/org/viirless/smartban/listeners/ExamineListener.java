package org.viirless.smartban.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.viirless.smartban.BanPlugin;

public class ExamineListener implements Listener {
    private final BanPlugin plugin;

    public ExamineListener(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.contains("Examining")) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        if (event.getCurrentItem().getType() == Material.REDSTONE) {
            Player staff = (Player) event.getWhoClicked();
            String targetName = title.substring(title.lastIndexOf(" ") + 1);

            // Close the examine GUI
            staff.closeInventory();

            // Execute the history command
            Bukkit.dispatchCommand(staff, "history " + targetName);
        }
    }
}
