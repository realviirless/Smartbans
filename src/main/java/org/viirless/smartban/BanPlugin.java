package org.viirless.smartban;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

public class BanPlugin extends JavaPlugin {

    private File bansFile;
    private FileConfiguration bansConfig;

    @Override
    public void onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Create bans.yml file for storing active bans
        createBansFile();

        // Create TabCompleter instance
        BanTabCompleter tabCompleter = new BanTabCompleter(this);

        // Register commands and tab completers
        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this));
        getCommand("kick").setExecutor(new KickCommand(this));
        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("unmute").setExecutor(new UnmuteCommand(this));

        // Register tab completers
        getCommand("ban").setTabCompleter(tabCompleter);
        getCommand("mute").setTabCompleter(tabCompleter);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

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
}