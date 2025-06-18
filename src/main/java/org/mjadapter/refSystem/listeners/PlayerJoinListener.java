package org.mjadapter.refSystem.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.mjadapter.refSystem.ReferralSystem;
import org.mjadapter.refSystem.managers.DataManager;

public class PlayerJoinListener implements Listener {
    private final ReferralSystem plugin;
    private final DataManager dataManager;

    public PlayerJoinListener(ReferralSystem plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore() && !dataManager.hasUsedCode(player.getUniqueId())) {
            player.sendMessage(getMessage("first-join-reminder"));
        }
    }

    private String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages." + path, ""));
    }
}