package org.mjadapter.refSystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mjadapter.refSystem.commands.RefCodeCommand;
import org.mjadapter.refSystem.commands.RefCommand;
import org.mjadapter.refSystem.database.DatabaseManager;
import org.mjadapter.refSystem.managers.DataManager;
import org.mjadapter.refSystem.managers.RewardManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

public class ReferralSystem extends JavaPlugin {
    private DataManager dataManager;
    private RewardManager rewardManager;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {

        try {
            saveDefaultConfig();
            reloadConfig();
            // Сохраняем конфиги
            saveResource("config.yml", false);


            // Инициализация менеджеров
            this.databaseManager = new DatabaseManager(this);
            databaseManager.init();
            this.dataManager = new DataManager(this);
            this.rewardManager = new RewardManager(this);


            // Форсированная регистрация команд
            registerCommandForcibly("ref", new RefCommand(this));
            registerCommandForcibly("refcode", new RefCodeCommand(this));

            getLogger().info("Плагин успешно запущен! Команды зарегистрированы");

        } catch (Exception e) {
            getLogger().severe("КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage());
            Arrays.stream(e.getStackTrace()).forEach(st -> getLogger().severe(st.toString()));
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void registerCommandForcibly(String name, CommandExecutor executor) throws Exception {
        // 1. Пытаемся получить команду стандартным способом
        PluginCommand command = getCommand(name);

        // 2. Если не получилось - создаем вручную
        if (command == null) {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            command = constructor.newInstance(name, this);

            // Регистрируем в CommandMap
            Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getPluginManager());

            commandMap.register(this.getName(), command);
            getLogger().warning("Команда /" + name + " зарегистрирована через reflection");
        }

        // Настраиваем команду
        command.setExecutor(executor);
        command.setPermission("refsystem.use");
        command.setPermissionMessage(ChatColor.RED + "Нет прав!");
    }

    // Геттеры
    public DataManager getDataManager() { return dataManager; }
    public RewardManager getRewardManager() { return rewardManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
}