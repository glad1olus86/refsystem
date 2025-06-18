package org.mjadapter.refSystem.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.mjadapter.refSystem.ReferralSystem;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private final ReferralSystem plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(ReferralSystem plugin) {
        this.plugin = plugin;
    }

    public void init() throws SQLException {
        DatabaseConfig config = new DatabaseConfig(plugin.getConfig());

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());

        // Оптимизации для MySQL
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(hikariConfig);

        // Тестовое подключение
        try (Connection conn = dataSource.getConnection()) {
            plugin.getLogger().info("Успешное подключение к MySQL!");
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
    public boolean isConnectionValid() {
        try (Connection conn = getConnection()) {
            return conn.isValid(2); // Проверка за 2 секунды
        } catch (SQLException e) {
            return false;
        }
    }
}