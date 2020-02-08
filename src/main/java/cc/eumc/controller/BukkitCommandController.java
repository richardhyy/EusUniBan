package cc.eumc.controller;

import cc.eumc.UniBanBukkitPlugin;
import cc.eumc.config.BukkitConfig;

import java.util.List;
import java.util.UUID;

public class BukkitCommandController extends CommandController {
    UniBanBukkitPlugin plugin;
    public BukkitCommandController(UniBanBukkitPlugin instance, BukkitConfig bukkitConfig) {
        super(bukkitConfig);
        this.plugin = instance;
    }

    @Override
    void doReload() {
        plugin.reloadConfig();
        plugin.reloadController();
        plugin.registerTask();
    }

    @Override
    boolean isBannedOnline(UUID uuid) {
        return plugin.getController().isBannedOnline(uuid);
    }

    @Override
    List<String> getBannedServerList(UUID uuid) {
        return plugin.getController().getBannedServerList(uuid);
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
