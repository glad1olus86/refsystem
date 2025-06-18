package org.mjadapter.refSystem.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mjadapter.refSystem.ReferralSystem;
import org.mjadapter.refSystem.managers.DataManager;

import java.util.UUID;

public class RefCodeCommand implements CommandExecutor {
    private final ReferralSystem plugin;
    private final DataManager dataManager;

    public RefCodeCommand(ReferralSystem plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Только для игроков!");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        String code = dataManager.getPlayerCode(playerId);
        if (code == null) {
            code = generateCode();
            dataManager.setPlayerCode(playerId, code);
        }

        player.sendMessage(getMessage("your-code").replace("%code%", code));
        return true;
    }

    private String generateCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages." + path, ""));
    }
}