package cc.eumc.uniban.listener;

import cc.eumc.uniban.UniBanBukkitPlugin;
import cc.eumc.uniban.controller.AccessController;
import cc.eumc.uniban.controller.UniBanController;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.net.InetSocketAddress;

public class PaperListener implements Listener {
    UniBanBukkitPlugin plugin;
    AccessController accessController = new AccessController();

    public PaperListener(UniBanBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onListPing(PaperServerListPingEvent e) {
        InetSocketAddress address = e.getClient().getVirtualHost();
        if (address == null) {
            return;
        }
        if (!address.getHostName().equals("UNIBAN")) {
            return;
        }

        String clientHost = e.getClient().getAddress().getHostString();
        if (!accessController.canAccess(clientHost)) {
            // if host was blocked
            e.setCancelled(true);
            return;
        }

        UniBanController controller = plugin.getController();
        controller.sendInfo("Ban-list request from: " + clientHost);
        e.setMotd(controller.getBanListJson());
    }
}
