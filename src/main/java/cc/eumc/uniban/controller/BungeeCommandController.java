package cc.eumc.uniban.controller;

import cc.eumc.uniban.UniBanBungeePlugin;
import cc.eumc.uniban.config.BungeeConfig;

import java.util.List;
import java.util.UUID;

public class BungeeCommandController extends CommandController {
    UniBanBungeePlugin plugin;
    public BungeeCommandController(UniBanBungeePlugin instance, BungeeConfig bungeeConfig) {
        super(bungeeConfig);
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
