package cc.eumc.uniban.listener;

import cc.eumc.uniban.UniBanBungeePlugin;
import cc.eumc.uniban.config.BungeeConfig;
import cc.eumc.uniban.config.Message;
import cc.eumc.uniban.config.PluginConfig;
import cc.eumc.uniban.controller.AccessController;
import cc.eumc.uniban.controller.UniBanController;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.net.InetSocketAddress;
import java.util.UUID;

public class BungeePlayerListener implements Listener {
    final UniBanBungeePlugin plugin;
    AccessController accessController = new AccessController();

    public BungeePlayerListener(UniBanBungeePlugin instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onPlayerLogin(LoginEvent e) {
        if (!e.getConnection().isOnlineMode())
            return;

        UUID uuid = e.getConnection().getUniqueId();
        if (PluginConfig.UUIDWhitelist.contains(uuid.toString())) {
            return;
        }

        int count = plugin.getController().getBannedServerAmount(e.getConnection().getUniqueId());
        if (BungeeConfig.WarnThreshold > 0 && count >= BungeeConfig.WarnThreshold) {
            String warningMessage = Message.MessagePrefix + Message.WarningMessage
                    .replace("{player}", e.getConnection().getName())
                    .replace("{uuid}", e.getConnection().getUniqueId().toString())
                    .replace("{number}", String.valueOf(count));
            plugin.getLogger().info(warningMessage);

            plugin.getProxy().getPlayers().forEach(proxiedPlayer -> {
                // TODO Fix: Warning spam
                if ((BungeeConfig.BroadcastWarning && proxiedPlayer.getUniqueId() != e.getConnection().getUniqueId())
                || (proxiedPlayer.hasPermission("uniban.getnotified") || proxiedPlayer.hasPermission("uniban.admin"))) {
                    proxiedPlayer.sendMessage(warningMessage);
                }
            });
        }
        if (BungeeConfig.BanThreshold > 0 && count >= BungeeConfig.BanThreshold) {
            BaseComponent[] reason = TextComponent.fromLegacyText(Message.BannedOnlineKickMessage.replace("{number}", String.valueOf(count)));
            e.getConnection().disconnect(reason);
            e.setCancelled(true);
            e.setCancelReason(reason);
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onListPing(ProxyPingEvent e) {
        InetSocketAddress address = e.getConnection().getVirtualHost();
        if (address == null) {
            return;
        }
        if (!address.getHostName().equals("UNIBAN")) {
            return;
        }

        String host = e.getConnection().getAddress().getHostString();
        if (!accessController.canAccess(host)) {
            // if host was blocked
            return;
        }

        UniBanController controller = plugin.getController();
        controller.sendInfo("Ban-list request from: " + host);
        ServerPing response = e.getResponse();
        response.setDescriptionComponent(new TextComponent(controller.getBanListJson()));
        e.setResponse(response);
    }
}
