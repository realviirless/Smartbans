package org.viirless.smartban;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BanPlugin extends JavaPlugin {

    private File bansFile;
    private FileConfiguration bansConfig;
    private File historyFile;
    private FileConfiguration historyConfig;
    private Set<UUID> frozenPlayers;
    private Set<UUID> vanishedPlayers; // Add this field

    @Override
    public void onEnable() {
        // Initialize sets
        frozenPlayers = new HashSet<>();
        vanishedPlayers = new HashSet<>(); // Initialize vanished players set

        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Create bans.yml and history.yml files
        createBansFile();
        createHistoryFile();

        // Create TabCompleter instance
        BanTabCompleter tabCompleter = new BanTabCompleter(this);

        // Register commands and tab completers
        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this));
        getCommand("kick").setExecutor(new KickCommand(this));
        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("unmute").setExecutor(new UnmuteCommand(this));
        getCommand("history").setExecutor(new HistoryCommand(this));
        getCommand("smartbans").setExecutor(new ReloadCommand(this));
        getCommand("freeze").setExecutor(new FreezeCommand(this));
        getCommand("unfreeze").setExecutor(new UnfreezeCommand(this));
        getCommand("examine").setExecutor(new ExamineCommand(this));
        getCommand("vanish").setExecutor(new VanishCommand(this));
        getCommand("invsee").setExecutor(new InvseeCommand(this));
        getCommand("clearinv").setExecutor(new ClearInventoryCommand(this));

        // Register tab completers
        getCommand("ban").setTabCompleter(tabCompleter);
        getCommand("mute").setTabCompleter(tabCompleter);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerFreezeListener(this), this);
        getServer().getPluginManager().registerEvents(new ExamineListener(this), this);

        getLogger().info("Ban Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Ban Plugin has been disabled!");
    }

    private void createBansFile() {
        bansFile = new File(getDataFolder(), "bans.yml");
        if (!bansFile.exists()) {
            bansFile.getParentFile().mkdirs();
            try {
                bansFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create bans.yml file!");
                e.printStackTrace();
            }
        }
        bansConfig = YamlConfiguration.loadConfiguration(bansFile);
    }

    private void createHistoryFile() {
        historyFile = new File(getDataFolder(), "history.yml");
        if (!historyFile.exists()) {
            saveResource("history.yml", false);
        }
        historyConfig = YamlConfiguration.loadConfiguration(historyFile);
    }

    public FileConfiguration getBansConfig() {
        return bansConfig;
    }

    public void saveBansConfig() {
        try {
            bansConfig.save(bansFile);
        } catch (IOException e) {
            getLogger().severe("Could not save bans.yml!");
            e.printStackTrace();
        }
    }

    public void reloadBansConfig() {
        bansConfig = YamlConfiguration.loadConfiguration(bansFile);
    }

    public FileConfiguration getHistoryConfig() {
        return historyConfig;
    }

    public void saveHistoryConfig() {
        try {
            historyConfig.save(historyFile);
        } catch (IOException e) {
            getLogger().severe("Could not save history.yml!");
            e.printStackTrace();
        }
    }

    public void reloadHistoryConfig() {
        if (historyFile == null) {
            historyFile = new File(getDataFolder(), "history.yml");
        }
        historyConfig = YamlConfiguration.loadConfiguration(historyFile);
    }

    public void addToHistory(String uuid, String type, String by, String reason, long duration) {
        String key = uuid + "." + System.currentTimeMillis();
        historyConfig.set(key + ".type", type);
        historyConfig.set(key + ".by", by);
        historyConfig.set(key + ".reason", reason);
        historyConfig.set(key + ".date", System.currentTimeMillis());
        historyConfig.set(key + ".duration", duration);
        saveHistoryConfig();
    }

    public Set<UUID> getFrozenPlayers() {
        return frozenPlayers;
    }

    public Set<UUID> getVanishedPlayers() {
        return vanishedPlayers;
    }

    public String colorize(String message) {
        return message != null ? org.bukkit.ChatColor.translateAlternateColorCodes('&', message) : "";
    }
}