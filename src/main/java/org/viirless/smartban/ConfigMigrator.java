package org.viirless.smartban;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigMigrator {
    private final BanPlugin plugin;
    private static final String CONFIG_VERSION = "config-version";
    private static final double CURRENT_VERSION = 1.2; // Increment version number

    public ConfigMigrator(BanPlugin plugin) {
        this.plugin = plugin;
    }

    public void migrateConfigs() {
        migrateConfig();
        migrateBans();
        migrateHistory();
    }

    private void migrateConfig() {
        FileConfiguration config = plugin.getConfig();
        double version = config.getDouble(CONFIG_VERSION, 0.0);

        if (version < CURRENT_VERSION) {
            plugin.getLogger().info("Migrating config.yml from version " + version + " to " + CURRENT_VERSION);

            // Add all possible config entries with default values if they don't exist
            addDefaultIfMissing(config, "messages.ban-success", "&cPlayer &e{player} &chas been banned for &e{reason}");
            addDefaultIfMissing(config, "messages.unban-success", "&aPlayer &e{player} &ahas been unbanned");
            addDefaultIfMissing(config, "messages.mute-success", "&cPlayer &e{player} &chas been muted for &e{reason}");
            addDefaultIfMissing(config, "messages.unmute-success", "&aPlayer &e{player} &ahas been unmuted");
            addDefaultIfMissing(config, "messages.no-permission", "&cYou don't have permission to use this command!");
            addDefaultIfMissing(config, "messages.player-not-found", "&cPlayer not found!");
            addDefaultIfMissing(config, "messages.freeze.title", "&c&lYOU ARE FROZEN");
            addDefaultIfMissing(config, "messages.freeze.subtitle", "&eContact staff in Discord");
            addDefaultIfMissing(config, "messages.freeze.bypass", "&cThis player has bypass permissions!");
            addDefaultIfMissing(config, "messages.freeze.cannot-freeze-self", "&cYou cannot freeze yourself!");
            addDefaultIfMissing(config, "messages.vanish.actionbar", "&aVanish &a✔");
            addDefaultIfMissing(config, "messages.reload-success", "&aConfiguration reloaded successfully!");

            // Update version
            config.set(CONFIG_VERSION, CURRENT_VERSION);
            plugin.saveConfig();
            plugin.getLogger().info("Config migration complete!");
        }
    }

    private void addDefaultIfMissing(FileConfiguration config, String path, Object defaultValue) {
        if (!config.contains(path)) {
            config.set(path, defaultValue);
            plugin.getLogger().info("Added missing config entry: " + path);
        }
    }

    private void migrateBans() {
        FileConfiguration bans = plugin.getBansConfig();
        double version = bans.getDouble(CONFIG_VERSION, 0.0);

        if (version < CURRENT_VERSION) {
            plugin.getLogger().info("Migrating bans.yml from version " + version + " to " + CURRENT_VERSION);

            // Example of a bans migration:
            if (version < 1.0) {
                ConfigurationSection bannedPlayers = bans.getConfigurationSection("banned-players");
                if (bannedPlayers != null) {
                    for (String uuid : bannedPlayers.getKeys(false)) {
                        ConfigurationSection ban = bannedPlayers.getConfigurationSection(uuid);
                        if (ban != null && !ban.contains("by")) {
                            // Add missing information
                            ban.set("by", "CONSOLE");
                            ban.set("time", System.currentTimeMillis());
                        }
                    }
                }
            }

            bans.set(CONFIG_VERSION, CURRENT_VERSION);
            plugin.saveBansConfig();
            plugin.getLogger().info("Bans migration complete!");
        }
    }

    private void migrateHistory() {
        FileConfiguration history = plugin.getHistoryConfig();
        double version = history.getDouble(CONFIG_VERSION, 0.0);

        if (version < CURRENT_VERSION) {
            plugin.getLogger().info("Migrating history.yml from version " + version + " to " + CURRENT_VERSION);

            // Example of a history migration:
            if (version < 1.0) {
                for (String uuid : history.getKeys(false)) {
                    if (!uuid.equals(CONFIG_VERSION)) {
                        ConfigurationSection playerHistory = history.getConfigurationSection(uuid);
                        if (playerHistory != null) {
                            for (String timeStamp : playerHistory.getKeys(false)) {
                                ConfigurationSection entry = playerHistory.getConfigurationSection(timeStamp);
                                if (entry != null && !entry.contains("date")) {
                                    // Convert timestamp to a date entry
                                    try {
                                        long time = Long.parseLong(timeStamp);
                                        entry.set("date", time);
                                    } catch (NumberFormatException ignored) {
                                        // Ignore invalid timestamps
                                    }
                                }
                            }
                        }
                    }
                }
            }

            history.set(CONFIG_VERSION, CURRENT_VERSION);
            plugin.saveHistoryConfig();
            plugin.getLogger().info("History migration complete!");
        }
    }
}
