package cc.eumc.listener;

import cc.eumc.UniBanBukkitPlugin;
import cc.eumc.config.PluginConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class BukkitPlayerListener implements Listener {
    final UniBanBukkitPlugin plugin;
    public BukkitPlayerListener(UniBanBukkitPlugin instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        if (PluginConfig.UUIDWhitelist.contains(e.getPlayer().getUniqueId().toString())) {
            return;
        }
        if (plugin.getController().isBannedOnline(e.getPlayer())) {
            if (e.getPlayer().isOp()) {
                plugin.getLogger().info("Ignored OP: " + e.getPlayer().getName());
                return;
            }
            e.disallow(PlayerLoginEvent.Result.KICK_BANNED, PluginConfig.BannedOnlineKickMessage.replace("{number}", plugin.getController().getBannedServerAmount(e.getPlayer()).toString()));
        }
    }
}
