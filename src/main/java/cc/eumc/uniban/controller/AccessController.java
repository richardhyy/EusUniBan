package cc.eumc.uniban.controller;

import cc.eumc.uniban.config.PluginConfig;

import java.util.HashMap;
import java.util.Map;

public class AccessController {
    Map<String, Long> lastAccessMap = new HashMap<>();
    int MinPeriodPerServer;

    public AccessController() {
        this.MinPeriodPerServer = PluginConfig.MinPeriodPerServer;
    }

    /**
     * Manually define minimum requesting interval
     * @param minPeriodPerServer Unit: minute
     */
    public AccessController(double minPeriodPerServer) {
        this.MinPeriodPerServer = (int) (60 * minPeriodPerServer);
    }

    public boolean canAccess(String host) {
        if (PluginConfig.EnabledAccessControl) {
            if (PluginConfig.WhitelistEnabled && PluginConfig.Whitelist.contains(host)) {
                return true;
            }
            if (PluginConfig.BlacklistEnabled && (PluginConfig.Blacklist.contains(host) || PluginConfig.Blacklist.contains("*"))) {
                return false;
            }
            return canAccessByCheckFrequency(host);
        }
        return true;
    }

    public boolean canAccessByCheckFrequency(String host) {
        if (MinPeriodPerServer == 0) return true;

        final long now = System.currentTimeMillis() / 1000;
        final long lastAccess = lastAccessMap.getOrDefault(host, 0L);
        lastAccessMap.put(host, now);
        if (lastAccess != 0L) {
            return now - lastAccess >= MinPeriodPerServer;
        }
        else {
            return true;
        }
    }
}
