package org.viirless.smartban;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigMigrator {
    private final BanPlugin plugin;
    private static final String CONFIG_VERSION = "config-version";
    private static final double CURRENT_VERSION = 1.5;

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
        plugin.getLogger().info("Current config version: " + version);
        boolean madeChanges = false;

        // Check and add basic settings
        if (!config.isSet("settings.use-id-system.ban")) {
            config.set("settings.use-id-system.ban", true);
            plugin.getLogger().info("Added settings.use-id-system.ban");
            madeChanges = true;
        }
        if (!config.isSet("settings.use-id-system.mute")) {
            config.set("settings.use-id-system.mute", true);
            plugin.getLogger().info("Added settings.use-id-system.mute");
            madeChanges = true;
        }

        // Check and add default ban reasons
        if (!config.isSet("bans.1")) {
            config.set("bans.1.reason", "Cheating/Hacking");
            config.set("bans.1.duration", "30d");
            madeChanges = true;
        }
        if (!config.isSet("bans.2")) {
            config.set("bans.2.reason", "Griefing");
            config.set("bans.2.duration", "7d");
            madeChanges = true;
        }
        // Add more default ban reasons
        if (!config.isSet("bans.3")) {
            config.set("bans.3.reason", "Spam/Advertising");
            config.set("bans.3.duration", "1h");
            madeChanges = true;
        }
        if (!config.isSet("bans.4")) {
            config.set("bans.4.reason", "Toxic Behavior");
            config.set("bans.4.duration", "3d");
            madeChanges = true;
        }
        if (!config.isSet("bans.5")) {
            config.set("bans.5.reason", "Exploiting");
            config.set("bans.5.duration", "14d");
            madeChanges = true;
        }

        // Check and add default mute reasons
        if (!config.isSet("mutes.1")) {
            config.set("mutes.1.reason", "Chat Spam");
            config.set("mutes.1.duration", "1h");
            madeChanges = true;
        }
        if (!config.isSet("mutes.2")) {
            config.set("mutes.2.reason", "Insulting Players");
            config.set("mutes.2.duration", "2h");
            madeChanges = true;
        }
        if (!config.isSet("mutes.3")) {
            config.set("mutes.3.reason", "Racism");
            config.set("mutes.3.duration", "7d");
            madeChanges = true;
        }
        if (!config.isSet("mutes.4")) {
            config.set("mutes.4.reason", "Advertising");
            config.set("mutes.4.duration", "1d");
            madeChanges = true;
        }

        // Check and add all messages
        // Basic messages
        madeChanges |= checkAndAddMessage(config, "messages.ban-success", "&aSuccessfully banned &c{player} &afor &e{reason} &afor &c{duration}");
        madeChanges |= checkAndAddMessage(config, "messages.unban-success", "&aSuccessfully unbanned &c{player}");
        madeChanges |= checkAndAddMessage(config, "messages.kick-success", "&aSuccessfully kicked &c{player} &afor &e{reason}");
        madeChanges |= checkAndAddMessage(config, "messages.player-kicked", "&cYou have been kicked from the server!\n&cReason: &e{reason}");
        madeChanges |= checkAndAddMessage(config, "messages.player-not-found", "&cPlayer not found!");
        madeChanges |= checkAndAddMessage(config, "messages.player-not-online", "&cPlayer is not online!");
        madeChanges |= checkAndAddMessage(config, "messages.no-permission", "&cYou don't have permission to use this command!");
        madeChanges |= checkAndAddMessage(config, "messages.cannot-target-self", "&cYou cannot target yourself!");
        madeChanges |= checkAndAddMessage(config, "messages.staff-bypass", "&cYou cannot target this player - they have bypass permission!");

        // Ban related messages
        madeChanges |= checkAndAddMessage(config, "messages.player-banned", "&cYou are banned from this server!\n&cReason: &e{reason}\n&cExpires: &e{expires}");
        madeChanges |= checkAndAddMessage(config, "messages.already-banned", "&c{player} is already banned!");
        madeChanges |= checkAndAddMessage(config, "messages.not-banned", "&c{player} is not banned!");
        madeChanges |= checkAndAddMessage(config, "messages.invalid-ban-id", "&cInvalid ban ID! Available IDs: {ids}");

        // Mute related messages
        madeChanges |= checkAndAddMessage(config, "messages.mute-success", "&aSuccessfully muted &c{player} &afor &e{reason} &afor &c{duration}");
        madeChanges |= checkAndAddMessage(config, "messages.unmute-success", "&aSuccessfully unmuted &c{player}");
        madeChanges |= checkAndAddMessage(config, "messages.player-muted", "&cYou have been muted in this server!\n&cReason: &e{reason}\n&cExpires: &e{expires}");
        madeChanges |= checkAndAddMessage(config, "messages.already-muted", "&c{player} is already muted!");
        madeChanges |= checkAndAddMessage(config, "messages.not-muted", "&c{player} is not muted!");
        madeChanges |= checkAndAddMessage(config, "messages.invalid-mute-id", "&cInvalid mute ID! Available IDs: {ids}");

        // History messages
        madeChanges |= checkAndAddMessage(config, "messages.history.title", "&8History of {player}");
        madeChanges |= checkAndAddMessage(config, "messages.history.no-entries", "&cNo history entries found");
        madeChanges |= checkAndAddMessage(config, "messages.history.entry.ban", "&eBan");
        madeChanges |= checkAndAddMessage(config, "messages.history.entry.mute", "&eMute");
        madeChanges |= checkAndAddMessage(config, "messages.history.entry.by", "&7By: &f{staff}");
        madeChanges |= checkAndAddMessage(config, "messages.history.entry.reason", "&7Reason: &f{reason}");
        madeChanges |= checkAndAddMessage(config, "messages.history.entry.date", "&7Date: &f{date}");
        madeChanges |= checkAndAddMessage(config, "messages.history.entry.duration.permanent", "&7Duration: &fPermanent");
        madeChanges |= checkAndAddMessage(config, "messages.history.entry.duration.temporary", "&7Duration: &f{duration}");

        // Freeze messages
        madeChanges |= checkAndAddMessage(config, "messages.freeze.title.main", "&cYou have been frozen!");
        madeChanges |= checkAndAddMessage(config, "messages.freeze.title.subtitle", "&7Contact a staff member in the discord");
        madeChanges |= checkAndAddMessage(config, "messages.freeze.frozen-by", "&aYou have frozen {player}");
        madeChanges |= checkAndAddMessage(config, "messages.freeze.already-frozen", "&cThis player is already frozen");
        madeChanges |= checkAndAddMessage(config, "messages.freeze.not-frozen", "&cThis player is not frozen");
        madeChanges |= checkAndAddMessage(config, "messages.freeze.unfrozen", "&aYou have been unfrozen");
        madeChanges |= checkAndAddMessage(config, "messages.freeze.unfrozen-by", "&aYou have unfrozen {player}");

        // Vanish messages
        madeChanges |= checkAndAddMessage(config, "messages.vanish.enabled", "&aVanish mode enabled");
        madeChanges |= checkAndAddMessage(config, "messages.vanish.disabled", "&cVanish mode disabled");
        madeChanges |= checkAndAddMessage(config, "messages.vanish.actionbar", "&7Vanish &a✔");

        // Examine messages
        madeChanges |= checkAndAddMessage(config, "messages.examine.title", "&8Examining {player}");
        madeChanges |= checkAndAddMessage(config, "messages.examine.usage", "&cUsage: /examine <player>");
        madeChanges |= checkAndAddMessage(config, "messages.examine.offline", "&cPlayer must be online to be examined");

        // Chat lock messages
        madeChanges |= checkAndAddMessage(config, "messages.chat.locked", "&cThe chat has been locked by &e{staff}");
        madeChanges |= checkAndAddMessage(config, "messages.chat.unlocked", "&aThe chat has been unlocked by &e{staff}");
        madeChanges |= checkAndAddMessage(config, "messages.chat.no-permission-write", "&cThe chat is currently locked. Only staff members can write.");
        madeChanges |= checkAndAddMessage(config, "messages.chat.already-locked", "&cThe chat is already locked!");
        madeChanges |= checkAndAddMessage(config, "messages.chat.already-unlocked", "&cThe chat is already unlocked!");

        // Usage messages
        madeChanges |= checkAndAddMessage(config, "messages.usage-ban", "&cUsage: /ban <player> <id>");
        madeChanges |= checkAndAddMessage(config, "messages.usage-unban", "&cUsage: /unban <player>");
        madeChanges |= checkAndAddMessage(config, "messages.usage-kick", "&cUsage: /kick <player> <reason>");
        madeChanges |= checkAndAddMessage(config, "messages.usage-mute", "&cUsage: /mute <player> <mute-id>");
        madeChanges |= checkAndAddMessage(config, "messages.history-usage", "&cUsage: /history <player>");
        madeChanges |= checkAndAddMessage(config, "messages.freeze-usage", "&cUsage: /freeze <player>");
        madeChanges |= checkAndAddMessage(config, "messages.unfreeze-usage", "&cUsage: /unfreeze <player>");

        if (madeChanges || version < CURRENT_VERSION) {
            config.set(CONFIG_VERSION, CURRENT_VERSION);
            plugin.saveConfig();
            plugin.reloadConfig();
            plugin.getLogger().info("Config update complete!");
        }
    }

    private boolean checkAndAddMessage(FileConfiguration config, String path, String defaultValue) {
        if (!config.isSet(path)) {
            config.set(path, defaultValue);
            plugin.getLogger().info("Added " + path);
            return true;
        }
        return false;
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
