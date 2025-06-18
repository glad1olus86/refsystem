package org.mjadapter.refSystem.database;

import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseConfig {
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final int maxPoolSize;
    private final long connectionTimeout;
    private final boolean useSSL;

    public DatabaseConfig(FileConfiguration config) {
        // Основные параметры подключения
        this.host = config.getString("database.host", "sql7.freesqldatabase.com");
        this.port = config.getInt("database.port", 3306);
        this.database = config.getString("database.name", "sql7784781");
        this.username = config.getString("database.username", "sql7784781");
        this.password = config.getString("database.password", "2XnDYeumV9");
        this.useSSL = config.getBoolean("database.use-ssl", false);

        // Настройки пула соединений
        this.maxPoolSize = config.getInt("database.pool.max-size", 10);
        this.connectionTimeout = config.getLong("database.pool.connection-timeout", 30000);

        // Валидация обязательных параметров
        validateConfig();
    }

    private void validateConfig() {
        if (this.database == null || this.database.isEmpty()) {
            throw new IllegalArgumentException("Database name (database.name) must be specified in config.yml");
        }
        if (this.username == null || this.username.isEmpty()) {
            throw new IllegalArgumentException("Database username (database.username) must be specified in config.yml");
        }
    }

    /**
     * Формирует JDBC URL для подключения к MySQL
     */
    public String getJdbcUrl() {
        return String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=%b&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                host, port, database, useSSL
        );
    }

    // Геттеры
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public boolean isUseSSL() {
        return useSSL;
    }
}