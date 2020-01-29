package cc.eumc.listener;

import cc.eumc.PluginConfig;
import cc.eumc.UniBanPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerListener implements Listener {
    final UniBanPlugin plugin;
    public PlayerListener(UniBanPlugin instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        if (PluginConfig.UUIDWhitelist.contains(e.getPlayer().getUniqueId().toString())) {
            return;
        }
        if (plugin.isBannedOnline(e.getPlayer())) {
            if (e.getPlayer().isOp()) {
                plugin.getLogger().info("Ignored OP: " + e.getPlayer().getName());
                return;
            }
            e.disallow(PlayerLoginEvent.Result.KICK_BANNED, PluginConfig.BannedOnlineKickMessage.replace("{number}", plugin.getBannedServerAmount(e.getPlayer()).toString()));
        }
    }
}
