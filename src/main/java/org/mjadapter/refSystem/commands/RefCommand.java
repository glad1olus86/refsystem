package org.mjadapter.refSystem.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mjadapter.refSystem.ReferralSystem;
import org.mjadapter.refSystem.managers.DataManager;
import org.mjadapter.refSystem.managers.RewardManager;

import java.util.UUID;

public class RefCommand implements CommandExecutor {
    private final ReferralSystem plugin;
    private final DataManager dataManager;
    private final RewardManager rewardManager;

    public RefCommand(ReferralSystem plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.rewardManager = plugin.getRewardManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда только для игроков!");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        if (args.length == 0) {
            player.sendMessage(getMessage("usage-ref"));
            return true;
        }

        if (dataManager.hasUsedCode(playerId)) {
            player.sendMessage(getMessage("already-used"));
            return true;
        }

        UUID inviterId = dataManager.getPlayerByCode(args[0]);
        if (inviterId == null || inviterId.equals(playerId)) {
            player.sendMessage(getMessage("invalid-code"));
            return true;
        }

        // Регистрируем приглашение
        dataManager.setUsedCode(playerId, inviterId);
        dataManager.addInvitation(inviterId, playerId);

        // Выдаем награды
        rewardManager.checkAndGiveRewards(inviterId, playerId);

        // Уведомления
        player.sendMessage(getMessage("success-invited")
                .replace("%inviter%", Bukkit.getOfflinePlayer(inviterId).getName()));

        Player inviter = Bukkit.getPlayer(inviterId);
        if (inviter != null) {
            inviter.sendMessage(getMessage("inviter-notify")
                    .replace("%invited%", player.getName()));
        }

        return true;
    }

    private String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages." + path, "&cСообщение не найдено"));
    }
}