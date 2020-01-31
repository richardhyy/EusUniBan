package cc.eumc.listener;

import cc.eumc.UniBanBukkitPlugin;
import cc.eumc.config.BukkitConfig;
import cc.eumc.config.PluginConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
        if (PluginConfig.UUIDWhitelist.contains(e.getPlayer().getUniqueId().toString()) || e.getPlayer().hasPermission("uniban.ignore")) {
            return;
        }

        /* Replaced with hasPermission(uniban.ignore)
        if (e.getPlayer().isOp()) {
            plugin.getLogger().info("Ignored OP: " + e.getPlayer().getName());
            return;
        }*/
        int count = plugin.getController().getBannedServerAmount(e.getPlayer().getUniqueId());
        if (BukkitConfig.WarnThreshold > 0 && count >= BukkitConfig.WarnThreshold) {
            String warningMessage = BukkitConfig.WarningMessage
                    .replace("{player}", e.getPlayer().getName())
                    .replace("{uuid}", e.getPlayer().getUniqueId().toString())
                    .replace("{number}", String.valueOf(count));
            plugin.getLogger().info(warningMessage);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("uniban.getnotified") || p.hasPermission("uniban.admin")) {
                    p.sendMessage(warningMessage);
                }
            }
        }
        if (BukkitConfig.BanThreshold > 0 && count >= BukkitConfig.BanThreshold)
            e.disallow(PlayerLoginEvent.Result.KICK_BANNED, PluginConfig.BannedOnlineKickMessage.replace("{number}", String.valueOf(count)));
    }
}
