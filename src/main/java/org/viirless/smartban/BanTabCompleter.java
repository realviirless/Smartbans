package org.viirless.smartban;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BanTabCompleter implements TabCompleter {

    private final BanPlugin plugin;

    public BanTabCompleter(BanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            // Zeige Online-Spieler für das erste Argument
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(input)) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 2) {
            String input = args[1].toLowerCase();
            boolean useIdSystem = plugin.getConfig().getBoolean("settings.use-id-system." + command.getName(), true);
            String section = command.getName().equals("ban") ? "bans" : "mutes";
            ConfigurationSection reasons = plugin.getConfig().getConfigurationSection(section);

            if (reasons != null) {
                if (useIdSystem) {
                    // ID-System: Zeige IDs
                    completions.addAll(reasons.getKeys(false).stream()
                        .filter(id -> id.startsWith(input) ||
                                    reasons.getString(id + ".reason", "").toLowerCase().startsWith(input))
                        .collect(Collectors.toList()));
                } else {
                    // Direktes System: Zeige Gründe
                    completions.addAll(reasons.getKeys(false).stream()
                        .map(id -> reasons.getString(id + ".reason"))
                        .filter(reason -> reason != null && reason.toLowerCase().startsWith(input))
                        .collect(Collectors.toList()));
                }
            }
        }

        return completions;
    }
}
