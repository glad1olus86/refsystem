package org.mjadapter.refSystem.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mjadapter.refSystem.ReferralSystem;
import org.mjadapter.refSystem.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DataManager {
    private final ReferralSystem plugin;
    private final DatabaseManager databaseManager;

    public DataManager(ReferralSystem plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        createTablesIfNotExists();
    }

    private void createTablesIfNotExists() {
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Таблица реферальных кодов
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS referral_codes (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "code VARCHAR(16) UNIQUE NOT NULL, " +
                            "nickname VARCHAR(16), " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );

            // Таблица использованных кодов
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS used_codes (" +
                            "player_uuid VARCHAR(36) PRIMARY KEY, " +
                            "inviter_uuid VARCHAR(36) NOT NULL, " +
                            "used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );

            // Таблица приглашений
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS invitations (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "inviter_uuid VARCHAR(36) NOT NULL, " +
                            "invited_uuid VARCHAR(36) UNIQUE NOT NULL, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "FOREIGN KEY (inviter_uuid) REFERENCES referral_codes(uuid))"
            );

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка создания таблиц", e);
        }
    }

    // Основные методы:

    public String getPlayerCode(UUID uuid) {
        String sql = "SELECT code FROM referral_codes WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("code") : null;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка получения кода", e);
            return null;
        }
    }

    public void setPlayerCode(UUID uuid, String code) {
        String sql = "INSERT INTO referral_codes (uuid, code, nickname) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE code = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            Player player = Bukkit.getPlayer(uuid);
            String nickname = player != null ? player.getName() : null;

            stmt.setString(1, uuid.toString());
            stmt.setString(2, code);
            stmt.setString(3, nickname); // Добавляем nickname
            stmt.setString(4, code);
            stmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка сохранения кода", e);
        }
    }

    public boolean hasUsedCode(UUID playerId) {
        String sql = "SELECT 1 FROM used_codes WHERE player_uuid = ? LIMIT 1";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerId.toString());
            return stmt.executeQuery().next();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка проверки кода", e);
            return false;
        }
    }

    public void setUsedCode(UUID playerId, UUID inviterId) {
        String sql = "INSERT INTO used_codes (player_uuid, inviter_uuid) VALUES (?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerId.toString());
            stmt.setString(2, inviterId.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка сохранения использованного кода", e);
        }
    }

    public UUID getInviter(UUID playerId) {
        String sql = "SELECT inviter_uuid FROM used_codes WHERE player_uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerId.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? UUID.fromString(rs.getString("inviter_uuid")) : null;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка получения пригласителя", e);
            return null;
        }
    }

    public void addInvitation(UUID inviterId, UUID invitedId) {
        String sql = "INSERT INTO invitations (inviter_uuid, invited_uuid) VALUES (?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, inviterId.toString());
            stmt.setString(2, invitedId.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка добавления приглашения", e);
        }
    }
    public void updateInviteTask(UUID uuid, String taskType, int progress, int required, boolean completed) {
        String sql = "INSERT INTO invite_tasks (uuid, task_type, progress, required, completed) " +
                "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "progress = VALUES(progress), completed = VALUES(completed)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid.toString());
            stmt.setString(2, taskType);
            stmt.setInt(3, progress);
            stmt.setInt(4, required);
            stmt.setBoolean(5, completed);
            stmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка обновления задачи", e);
        }
    }

    public UUID getPlayerByCode(String code) {
        String sql = "SELECT uuid FROM referral_codes WHERE code = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? UUID.fromString(rs.getString("uuid")) : null;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка поиска по коду", e);
            return null;
        }
    }

    public int getInvitedCount(UUID inviterId) {
        String sql = "SELECT COUNT(*) FROM invitations WHERE inviter_uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, inviterId.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка подсчета приглашений", e);
            return 0;
        }
    }
}