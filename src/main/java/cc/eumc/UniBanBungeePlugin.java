package cc.eumc;

import cc.eumc.command.BungeeCommand;
import cc.eumc.config.PluginConfig;
import cc.eumc.controller.UniBanBungeeController;
import cc.eumc.listener.BungeePlayerListener;
import cc.eumc.task.LocalBanListRefreshTask;
import cc.eumc.task.SubscriptionRefreshTask;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class UniBanBungeePlugin extends Plugin {
    ScheduledTask Task_LocalBanListRefreshTask;
    ScheduledTask Task_SubscriptionRefreshTask;
    UniBanBungeeController controller;
    Configuration configuration;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            saveDefaultConfig();
        }

        reloadConfig();

        controller = new UniBanBungeeController(this);

        registerTask();
        registerCommand();

        getProxy().getPluginManager().registerListener(this, new BungeePlayerListener(this));

        getLogger().info("UniBan Enabled");
    }

    private void saveDefaultConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = getResourceAsStream("config.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create config.yml", e);
            }
        }
    }

    @Override
    public void onDisable() {
        if (Task_LocalBanListRefreshTask != null)
            Task_LocalBanListRefreshTask.cancel();
        if (Task_SubscriptionRefreshTask != null)
            Task_SubscriptionRefreshTask.cancel();
        controller.destruct();
        getLogger().info("UniBan Disabled");
    }

    public void reloadConfig() {
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
            new PluginConfig();
        } catch (IOException e) {
            getProxy().getLogger().severe("Unable to load configuration.");
        }

    }

    public void reloadController() {
        if (controller != null)
            controller.destruct();
        controller = new UniBanBungeeController(this);
    }

    void registerCommand() {
        getProxy().getPluginManager().registerCommand(this, new BungeeCommand(this));
    }

    public void registerTask() {
        if (Task_LocalBanListRefreshTask != null)
            Task_LocalBanListRefreshTask.cancel();
        if (PluginConfig.EnableBroadcast)
            Task_LocalBanListRefreshTask = getProxy().getScheduler().schedule(this, new LocalBanListRefreshTask(getController()), 1,
                    (int) (60 * PluginConfig.LocalBanListRefreshPeriod), TimeUnit.SECONDS);

        if (Task_SubscriptionRefreshTask != null)
            Task_SubscriptionRefreshTask.cancel();
        Task_SubscriptionRefreshTask = getProxy().getScheduler().schedule(this, new SubscriptionRefreshTask(getController()), 20,
                (int) (60 * PluginConfig.SubscriptionRefreshPeriod), TimeUnit.SECONDS);

        // TODO run IdentifySubscriptionTask
    }

    public UniBanBungeeController getController() {
        return controller;
    }

    public Configuration getConfig() {
        return configuration;
    }

    public void saveConfig() {
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(getDataFolder(), "config.yml"));
    }

}
