package cc.eumc.uniban.task;

import cc.eumc.uniban.config.PluginConfig;
import cc.eumc.uniban.config.ThirdPartySupportConfig;
import cc.eumc.uniban.controller.UniBanController;
import cc.eumc.uniban.util.AdvancedBanSupport;
import cc.eumc.uniban.util.BungeeBanSupport;
import cc.eumc.uniban.util.LiteBansSupport;
import cc.eumc.uniban.util.VanillaListSupport;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
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
                BanEntry banEntry = Bukkit.getServer().getBanList(BanList.Type.NAME).getBanEntry(offlinePlayer.getName());
                if (banEntry != null) {
                    if (controller.shouldIgnoreReason(banEntry.getReason())) {
                        continue;
                    }
                }
                //if (offlinePlayer.hasPlayedBefore()) {
                uuidSet.add(offlinePlayer.getUniqueId());
                //}
            }
        }

        if (ThirdPartySupportConfig.AdvancedBan) {
            uuidSet.addAll(AdvancedBanSupport.fetchAllBanned(controller));
        }
        // TODO Support for BungeeAdminTool
        /*if (ThirdPartySupportConfig.BungeeAdminTool) {
            uuidSet.addAll(BungeeAdminToolSupport.fetchAllBanned(controller));
        }*/
        if (ThirdPartySupportConfig.BungeeBan) {
            uuidSet.addAll(BungeeBanSupport.fetchAllBanned(controller));
        }
        if (ThirdPartySupportConfig.LiteBans) {
            uuidSet.addAll(LiteBansSupport.fetchAllBanned(controller));
        }
        if (ThirdPartySupportConfig.VanillaList) {
            uuidSet.addAll(VanillaListSupport.fetchAllBanned(controller));
        }

        controller.updateLocalBanListCache(uuidSet);

        if (PluginConfig.ActiveMode_Enabled) {
            controller.sendLocalBanListToURL();
        }

        running = false;
    }
}
