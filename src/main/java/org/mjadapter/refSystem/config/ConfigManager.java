package org.mjadapter.refSystem.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public String getMessage(String path) {
        return config.getString("messages." + path, "Сообщение не найдено");
    }

    // Пример других параметров
    public String getDiscordClientId() {
        return config.getString("discord.client_id");
    }

    public String getDatabaseHost() {
        return config.getString("database.host");
    }
}