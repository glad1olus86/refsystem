package org.mjadapter.refSystem.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.mjadapter.refSystem.ReferralSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class RewardManager {
    private final ReferralSystem plugin;

    public RewardManager(ReferralSystem plugin) {
        this.plugin = plugin;
    }
    private void markRewardAsGiven(Connection conn, UUID inviterId, int level) throws SQLException {
        String sql = "INSERT INTO referral_rewards (uuid, reward_level) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, inviterId.toString());
            stmt.setInt(2, level);
            stmt.executeUpdate();
            plugin.getLogger().info("Награда уровня " + level + " зарегистрирована для " + inviterId);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка регистрации награды", e);
            throw e;
        }
    }

    public void checkAndGiveRewards(UUID inviterId, UUID invitedId) {
        try {
            // Даем стандартную награду за приглашение
            giveInviteReward(inviterId, invitedId);

            // Проверяем уровни наград
            checkLevelRewards(inviterId);
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка выдачи награды: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void giveInviteReward(UUID inviterId, UUID invitedId) {
        // Базовая награда за каждое приглашение
        Player inviter = Bukkit.getPlayer(inviterId);
        if (inviter != null && inviter.isOnline()) {
            inviter.sendMessage(ChatColor.GREEN + "Спасибо за приглашение игрока!");
        }
    }

    private void checkLevelRewards(UUID inviterId) {
        ConfigurationSection rewards = plugin.getConfig().getConfigurationSection("rewards.levels");
        if (rewards == null) {
            plugin.getLogger().warning("Раздел rewards.levels не найден в config.yml!");
            return;
        }

        int invitedCount = plugin.getDataManager().getInvitedCount(inviterId);
        Player inviter = Bukkit.getPlayer(inviterId);
        if (inviter == null) return;

        for (String levelStr : rewards.getKeys(false)) {
            try {
                int level = Integer.parseInt(levelStr);
                if (invitedCount == level) {
                    ConfigurationSection levelReward = rewards.getConfigurationSection(levelStr);
                    if (levelReward != null) {
                        giveLevelReward(inviter, levelReward);
                    }
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Некорректный уровень награды: " + levelStr);
            }
        }
    }

    private void giveLevelReward(Player inviter, ConfigurationSection rewardConfig) {
        // Выполняем команды
        List<String> commands = rewardConfig.getStringList("commands");
        if (commands != null) {
            for (String cmd : commands) {
                try {
                    String processedCmd = cmd.replace("{inviter}", inviter.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCmd);
                } catch (Exception e) {
                    plugin.getLogger().severe("Ошибка выполнения команды: " + cmd);
                }
            }
        }


        // Отправляем сообщение
        String message = rewardConfig.getString("message");
        if (message != null) {
            inviter.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}