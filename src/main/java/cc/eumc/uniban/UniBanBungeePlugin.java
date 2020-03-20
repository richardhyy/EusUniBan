package cc.eumc.uniban;

import cc.eumc.uniban.command.BungeeCommand;
import cc.eumc.uniban.config.*;
import cc.eumc.uniban.controller.UniBanBungeeController;
import cc.eumc.uniban.listener.BungeePlayerListener;
import cc.eumc.uniban.task.LocalBanListRefreshTask;
import cc.eumc.uniban.task.SubscriptionRefreshTask;
import cc.eumc.uniban.task.UpdateCheckTask;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class UniBanBungeePlugin extends Plugin {
    static UniBanBungeePlugin instance;
    ScheduledTask Task_LocalBanListRefreshTask;
    ScheduledTask Task_SubscriptionRefreshTask;
    UniBanBungeeController controller;
    Configuration configuration;
    BungeeConfig bungeeConfig;

    @Override
    public void onEnable() {
        instance = this;
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        reloadConfig();

        controller = new UniBanBungeeController();

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
        // Fix: Error when config was deleted before reloading
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            saveDefaultConfig();
        }

        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            getProxy().getLogger().severe("Unable to load configuration.");
        }

        // Fix: Configuration will not be reloaded on Bungeecord
        bungeeConfig = new BungeeConfig(this);

        showPluginInformation();
    }

    public void showPluginInformation() {
        getLogger().info(String.format(Message.SubscriptionsHeader, BukkitConfig.Subscriptions.size()));
        for (ServerEntry serverEntry : BungeeConfig.Subscriptions.keySet()) {
            getLogger().info("* " + serverEntry.getAddress() + (BungeeConfig.Subscriptions.get(serverEntry.getAddress())!=null?" | "+Message.Encrypted:""));
        }

        getLogger().info(Message.ThirdPartyPluginSupportHeader);
        getLogger().info("* AdvancedBan: " + (ThirdPartySupportConfig.AdvancedBan?Message.PluginEnabled:Message.PluginNotFound));
        //getLogger().info("* BungeeAdminTool: " + (ThirdPartySupportConfig.BungeeAdminTool?Message.PluginEnabled:Message.PluginNotFound));
        getLogger().info("* BungeeBan: " + (ThirdPartySupportConfig.BungeeBan?Message.PluginEnabled: Message.PluginNotFound));
        getLogger().info("* LiteBans: " + (ThirdPartySupportConfig.LiteBans?Message.PluginEnabled: Message.PluginNotFound));
        getLogger().info("* VanillaList: " + (ThirdPartySupportConfig.VanillaList?Message.PluginEnabled: Message.PluginNotFound));
    }

    public void reloadController() {
        if (controller != null)
            controller.destruct();
        controller = new UniBanBungeeController();
    }

    void registerCommand() {
        getProxy().getPluginManager().registerCommand(this, new BungeeCommand(this));
    }

    public void registerTask() {
        // T√ODO Third-party Bungeecord ban-list plugin support
        if (Task_LocalBanListRefreshTask != null)
            Task_LocalBanListRefreshTask.cancel();
        if (PluginConfig.EnableBroadcast)
            Task_LocalBanListRefreshTask = getProxy().getScheduler().schedule(this, new LocalBanListRefreshTask(getController(),true), 1,
                    (int) (60 * PluginConfig.LocalBanListRefreshPeriod), TimeUnit.SECONDS);

        if (Task_SubscriptionRefreshTask != null)
            Task_SubscriptionRefreshTask.cancel();
        Task_SubscriptionRefreshTask = getProxy().getScheduler().schedule(this, new SubscriptionRefreshTask(getController()), 20,
                (int) (60 * PluginConfig.SubscriptionRefreshPeriod), TimeUnit.SECONDS);

        getProxy().getScheduler().runAsync(this, new UpdateCheckTask(getDescription().getVersion(), 74747));

        // TODO run IdentifySubscriptionTask
    }

    public UniBanBungeeController getController() {
        return controller;
    }

    public Configuration getConfig() {
        return configuration;
    }

    public void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("Failed saving configuration file.");
        }
    }

    public static UniBanBungeePlugin getInstance() {
        return instance;
    }

    public BungeeConfig getBungeeConfig() {
        return bungeeConfig;
    }
}
