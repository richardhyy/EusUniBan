package cc.eumc.uniban.task;

import cc.eumc.uniban.config.ThirdPartySupportConfig;
import cc.eumc.uniban.controller.UniBanController;
import cc.eumc.uniban.util.AdvancedBanSupport;
import cc.eumc.uniban.util.BungeeBanSupport;
import cc.eumc.uniban.util.LiteBansSupport;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LocalBanListRefreshTask implements Runnable {
    final UniBanController controller;

    boolean isBungee;

    boolean running = false;

    public LocalBanListRefreshTask(UniBanController instance, boolean isBungee) {
        this.controller = instance;
        this.isBungee = isBungee;
    }

    @Override
    public void run() {
        if (running) {
            return;
        }
        running = true;

        Set<UUID> uuidSet = new HashSet<>();

        if (!isBungee) {
            for (OfflinePlayer offlinePlayer : Bukkit.getServer().getBannedPlayers()) {
                //if (offlinePlayer.hasPlayedBefore()) {
                uuidSet.add(offlinePlayer.getUniqueId());
                //}
            }
        }

        if (ThirdPartySupportConfig.AdvancedBan) {
            uuidSet.addAll(AdvancedBanSupport.fetchAllBanned());
        }
        // TODO Support for BungeeAdminTool
        /*if (ThirdPartySupportConfig.BungeeAdminTool) {
            uuidSet.addAll(BungeeAdminToolSupport.fetchAllBanned());
        }*/
        if (ThirdPartySupportConfig.BungeeBan) {
            uuidSet.addAll(BungeeBanSupport.fetchAllBanned());
        }
        if (ThirdPartySupportConfig.LiteBans) {
            uuidSet.addAll(LiteBansSupport.fetchAllBanned());
        }

        controller.updateLocalBanListCache(uuidSet);
        running = false;
    }
}
