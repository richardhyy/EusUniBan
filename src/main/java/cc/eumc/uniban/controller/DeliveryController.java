package cc.eumc.uniban.controller;

import cc.eumc.uniban.config.PluginConfig;
import de.cgrotz.kademlia.Kademlia;
import de.cgrotz.kademlia.config.UdpListener;
import de.cgrotz.kademlia.node.Key;
import de.cgrotz.kademlia.node.Node;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class DeliveryController {
    static boolean ready = false;
    Kademlia kad;
    UniBanController controller;

    public DeliveryController(UniBanController controller) {
        this.controller = controller;

        // TODO DHT
        // In production the nodeId would preferably be static for one node
        kad = new Kademlia(Key.build(PluginConfig.NodeID), PluginConfig.Host+":"+PluginConfig.Port);
        // Bootstrap using a remote server (there is no special configuration on the remote server necessary)
        controller.runTaskLater(() -> {
            controller.sendInfo("[DHT] Preparing bootstrap");
            List<UdpListener> udpListenerCollection = new ArrayList<>();
            PluginConfig.Subscriptions.forEach((serverEntry, key) -> {
                udpListenerCollection.add(new UdpListener(serverEntry.host, serverEntry.port));
            });
            kad.bootstrap(Node.builder().advertisedListeners(udpListenerCollection).build());
            controller.sendInfo("[DHT] Ready");
            ready = true;
        }, 1);
    }

    /**
     * Get value from the DHT
     * @param key Key.build()
     * @return value decoded with base64 or null if not ready
     */
    public String get(Key key) {
        if (!ready) return null;

        return new String(Base64.getDecoder().decode(kad.get(key).getBytes()));
    }

    /**
     * Put value into the DHT
     * @param key Key.build()
     * @param value will be encoded with base64
     * @return false: not ready
     */
    public boolean put(Key key, String value) {
        if (!ready) return false;

        kad.put(key, Base64.getEncoder().encodeToString(value.getBytes()));
        return true;
    }

    public static boolean isReady() {
        return ready;
    }
}
