package cc.eumc.listener;

import cc.eumc.UniBanBungeePlugin;
import cc.eumc.config.PluginConfig;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class BungeePlayerListener implements Listener {
    final UniBanBungeePlugin plugin;
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
        if (plugin.getController().isBannedOnline(uuid)) {
            e.setCancelReason(TextComponent.fromLegacyText(PluginConfig.BannedOnlineKickMessage.replace("{number}", plugin.getController().getBannedServerAmount(uuid).toString())));
        }
    }
}
