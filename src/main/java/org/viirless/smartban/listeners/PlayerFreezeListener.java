package org.viirless.smartban.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.viirless.smartban.BanPlugin;

public class PlayerFreezeListener implements Listener {
    private final BanPlugin plugin;

    public PlayerFreezeListener(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (plugin.getFrozenPlayers().contains(player.getUniqueId())) {
            // Only cancel if the player actually moved position (not just looked around)
            if (event.getTo().getX() != event.getFrom().getX() ||
                    event.getTo().getY() != event.getFrom().getY() ||
                    event.getTo().getZ() != event.getFrom().getZ()) {
                event.setCancelled(true);

                String mainTitle = plugin.getConfig().getString("messages.freeze.title.main");
                String subtitle = plugin.getConfig().getString("messages.freeze.title.subtitle");

                player.sendTitle(
                        plugin.colorize(mainTitle),
                        plugin.colorize(subtitle),
                        10,
                        60,
                        20);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Optional: Remove player from frozen set when they leave
        // Uncomment if you want frozen status to reset on logout
        // plugin.getFrozenPlayers().remove(event.getPlayer().getUniqueId());
    }
}
