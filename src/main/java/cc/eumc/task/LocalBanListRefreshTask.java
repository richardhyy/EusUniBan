package cc.eumc.task;

import cc.eumc.controller.UniBanController;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LocalBanListRefreshTask implements Runnable {
    final UniBanController controller;
    boolean running = false;
    public LocalBanListRefreshTask(UniBanController instance) {
        this.controller = instance;
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
        controller.updateLocalBanListCache(uuidSet);
        running = false;
    }
}
