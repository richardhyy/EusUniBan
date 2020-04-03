package cc.eumc.uniban.controller;

import cc.eumc.uniban.UniBanBungeePlugin;
import cc.eumc.uniban.config.BungeeConfig;
import cc.eumc.uniban.config.Message;
import net.md_5.bungee.config.Configuration;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class UniBanBungeeController extends UniBanController {

    public UniBanBungeeController() {
        super();

        // Fix: Broadcast status will not be displayed on BungeeCord
        if (super.serverStarted) {
            sendInfo(String.format(Message.BroadcastStarted, BungeeConfig.Host, BungeeConfig.Port, BungeeConfig.Threads));
        }
        else if (BungeeConfig.EnableBroadcast) {
            if (BungeeConfig.ActiveMode_Enabled) {
                sendInfo(Message.BroadcastActiveModeEnabled);
            }
            else {
                sendSevere(Message.BroadcastFailed);
            }
        }

    }

    @Override
    public File getDataFolder() {
        return UniBanBungeePlugin.getInstance().getDataFolder();
    }

    @Override
    public void sendInfo(String message) {
        UniBanBungeePlugin.getInstance().getLogger().info(message);
    }

    @Override
    public void sendWarning(String message) {
        UniBanBungeePlugin.getInstance().getLogger().warning(message);
    }

    @Override
    public void sendSevere(String message) {
        UniBanBungeePlugin.getInstance().getLogger().severe(message);
    }

    @Override
    public void saveConfig() {
        UniBanBungeePlugin.getInstance().saveConfig();
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

    public Configuration getConfig() {
        return UniBanBungeePlugin.getInstance().getConfig();
    }

    @Override
    public List<String> configGetStringList(String path) {
        return getConfig().getStringList(path);
    }

    @Override
    public boolean configIsSection(String path) {
        return getConfig().getSection(path)!=null;
    }

    @Override
    public Set<String> getConfigurationSectionKeys(String path) {
        return new HashSet<>(getConfig().getSection(path).getKeys());
    }

    @Override
    public void configSet(String path, Object object) {
        getConfig().set(path, object);
    }

    @Override
    public void runTaskLater(Runnable task, int delayTick) {
        UniBanBungeePlugin.getInstance().getProxy().getScheduler().schedule(UniBanBungeePlugin.getInstance(), task, (int)((float)(delayTick)/20), TimeUnit.SECONDS);
    }
}
