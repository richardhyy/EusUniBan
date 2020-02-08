package cc.eumc.controller;

import cc.eumc.UniBanBukkitPlugin;
import cc.eumc.config.BukkitConfig;
import cc.eumc.config.Message;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;
import java.util.Set;

public class UniBanBukkitController extends UniBanController {
    public UniBanBukkitController() {
        super();
        //this.plugin = instance;
        if (super.serverStarted) {
            sendInfo(String.format(Message.BroadcastStarted, BukkitConfig.Host, BukkitConfig.Port, BukkitConfig.Threads));
        }
        else if (BukkitConfig.EnableBroadcast) {
            sendSevere(Message.BroadcastFailed);
        }
    }

    @Override
    public File getDataFolder() {
        return UniBanBukkitPlugin.getInstance().getDataFolder();
    }

    @Override
    public void sendInfo(String message) {
        UniBanBukkitPlugin.getInstance().getLogger().info(message);
    }

    @Override
    public void sendWarning(String message) {
        UniBanBukkitPlugin.getInstance().getLogger().warning(message);
    }

    @Override
    public void sendSevere(String message) {
        UniBanBukkitPlugin.getInstance().getLogger().severe(message);
    }

    @Override
    public void saveConfig() {
        UniBanBukkitPlugin.getInstance().saveConfig();
    }

    @Override
    public boolean configGetBoolean(String path, Boolean def) {
        return getConfig().getBoolean(path, def);
    }

    @Override
    public String configGetString(String path, String def) {
        return getConfig().getString(path, def);
    }

    @Override
    public Double configGetDouble(String path, Double def) {
        return getConfig().getDouble(path, def);
    }

    @Override
    public int configGetInt(String path, int def) {
        return getConfig().getInt(path, def);
    }

    public FileConfiguration getConfig() {
        return UniBanBukkitPlugin.getInstance().getConfig();
    }

    @Override
    public List<String> configGetStringList(String path) {
        return UniBanBukkitPlugin.getInstance().getConfig().getStringList(path);
    }

    @Override
    public boolean configIsSection(String path) {
        return getConfig().isConfigurationSection(path);
    }

    @Override
    public Set<String> getConfigurationSectionKeys(String path) {
        return getConfig().getConfigurationSection(path).getKeys(false);
    }

    @Override
    public void configSet(String path, Object object) {
        UniBanBukkitPlugin.getInstance().getConfig().set(path, object);
    }
}
