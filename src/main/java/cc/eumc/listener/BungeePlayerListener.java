package cc.eumc.listener;

import cc.eumc.UniBanBungeePlugin;
import cc.eumc.config.BukkitConfig;
import cc.eumc.config.Message;
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

        int count = plugin.getController().getBannedServerAmount(e.getConnection().getUniqueId());
        if (BukkitConfig.WarnThreshold > 0 && count >= BukkitConfig.WarnThreshold) {
            String warningMessage = "[UniBan] " + Message.WarningMessage
                    .replace("{player}", e.getConnection().getName())
                    .replace("{uuid}", e.getConnection().getUniqueId().toString())
                    .replace("{number}", String.valueOf(count));
            plugin.getLogger().info(warningMessage);
        }
        if (BukkitConfig.BanThreshold > 0 && count >= BukkitConfig.BanThreshold)
            e.setCancelReason(TextComponent.fromLegacyText(Message.BannedOnlineKickMessage.replace("{number}", String.valueOf(count))));

    }
}
