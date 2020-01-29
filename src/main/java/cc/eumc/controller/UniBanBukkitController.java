package cc.eumc.controller;

import cc.eumc.UniBanBukkitPlugin;
import cc.eumc.config.PluginConfig;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;

public class UniBanBukkitController extends UniBanController {
    UniBanBukkitPlugin plugin;
    public UniBanBukkitController(UniBanBukkitPlugin instance) {
        super();
        this.plugin = instance;
        if (super.serverStarted) {
            sendInfo("UniBan broadcast started on " + PluginConfig.Host + ":" + PluginConfig.Port + " (" + PluginConfig.Threads + " Threads)");
        }
        else {
            sendSevere("Failed starting broadcast server");
        }
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public void saveConfig() {
        plugin.saveConfig();
    }

    @Override
    public void sendInfo(String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void sendWarning(String message) {
        plugin.getLogger().warning(message);
    }

    @Override
    public void sendSevere(String message) {
        plugin.getLogger().severe(message);
    }

    @Override
    List<String> getStringList(String path) {
        return plugin.getConfig().getStringList(path);
    }

    @Override
    void configSet(String path, Object object) {
        plugin.getConfig().set(path, object);
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }
}
