package cc.eumc.task;

import cc.eumc.UniBanPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LocalBanListRefreshTask implements Runnable {
    final UniBanPlugin plugin;
    boolean running = false;
    public LocalBanListRefreshTask(UniBanPlugin instance) {
        this.plugin = instance;
    }

    @Override
    public void run() {
        if (running) {
            return;
        }
        running = true;
        Set<UUID> uuidSet = new HashSet<>();
        for (OfflinePlayer offlinePlayer : Bukkit.getServer().getBannedPlayers()) {
            //if (offlinePlayer.hasPlayedBefore()) {
                uuidSet.add(offlinePlayer.getUniqueId());
            //}
        }
        plugin.updateLocalBanListCache(uuidSet);
        running = false;
    }
}
