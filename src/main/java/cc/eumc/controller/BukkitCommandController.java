package cc.eumc.controller;

import cc.eumc.UniBanBukkitPlugin;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitCommandController extends CommandController {
    UniBanBukkitPlugin plugin;
    public BukkitCommandController(UniBanBukkitPlugin instance) {
        this.plugin = instance;
    }

    @Override
    void doReload() {
        plugin.reloadConfig();
        plugin.reloadController();
        plugin.registerTask();
    }

    @Override
    boolean isBannedOnline(Player player) {
        return isBannedOnline(player.getUniqueId());
    }


    @Override
    boolean isBannedOnline(UUID uuid) {
        return plugin.getController().isBannedOnline(uuid);
    }

    @Override
    void addWhitelist(UUID uuid) {
        plugin.getController().addWhitelist(uuid);
    }

    @Override
    void removeWhitelist(UUID uuid) {
        plugin.getController().removeWhitelist(uuid);
    }
}
