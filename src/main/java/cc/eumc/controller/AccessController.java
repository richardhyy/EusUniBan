package cc.eumc.controller;

import cc.eumc.config.PluginConfig;

import java.util.HashMap;
import java.util.Map;

public class AccessController {
    Map<String, Long> lastAccessMap = new HashMap<>();

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
        final long now = System.currentTimeMillis() / 1000;
        final long lastAccess = lastAccessMap.getOrDefault(host, 0L);
        lastAccessMap.put(host, now);
        if (lastAccess != 0L) {
            return now - lastAccess >= PluginConfig.MinPeriodPerServer;
        }
        else {
            return true;
        }
    }
}
