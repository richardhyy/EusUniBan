package cc.eumc.uniban.listener;

import cc.eumc.uniban.UniBanBukkitPlugin;
import cc.eumc.uniban.config.BukkitConfig;
import cc.eumc.uniban.config.Message;
import cc.eumc.uniban.config.PluginConfig;
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
        if (PluginConfig.UUIDWhitelist.contains(e.getPlayer().getUniqueId().toString()) || (!e.getPlayer().isOp() && e.getPlayer().hasPermission("uniban.ignore"))) {
            return;
        }

        boolean warned = false;
        boolean banned = false;
        int count = plugin.getController().getBannedServerAmount(e.getPlayer().getUniqueId());
        if (BukkitConfig.WarnThreshold > 0 && count >= BukkitConfig.WarnThreshold) {
            warned = true;
            if (!(e.getPlayer().isOp() && PluginConfig.IgnoreOP)) {
                String warningMessage = Message.MessagePrefix + Message.WarningMessage
                        .replace("{player}", e.getPlayer().getName())
                        .replace("{uuid}", e.getPlayer().getUniqueId().toString())
                        .replace("{number}", String.valueOf(count));
                plugin.getLogger().info(warningMessage);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    // TODO Fix: Warning spam
                    if ((BukkitConfig.BroadcastWarning && p != e.getPlayer())
                    || (p.hasPermission("uniban.getnotified") || p.hasPermission("uniban.admin"))) {
                        p.sendMessage(warningMessage);
                    }
                }
            }
        }
        if (BukkitConfig.BanThreshold > 0 && count >= BukkitConfig.BanThreshold) {
            banned = true;
            if (!(e.getPlayer().isOp() && PluginConfig.IgnoreOP))
                e.disallow(PlayerLoginEvent.Result.KICK_BANNED, Message.BannedOnlineKickMessage.replace("{number}", String.valueOf(count)));
        }

        // OP check moved here because if it is bypassed before ban check, we will not know if one of the staff/opped players are banned somewhere
        if (PluginConfig.IgnoreOP && e.getPlayer().isOp() && (warned || banned)) {
            plugin.getLogger().info(Message.MessagePrefix + String.format(Message.IgnoredOP, e.getPlayer().getName()));
        }
    }
}
