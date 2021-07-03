package cc.eumc.uniban;

import cc.eumc.uniban.command.BukkitCommand;
import cc.eumc.uniban.config.*;
import cc.eumc.uniban.controller.UniBanBukkitController;
import cc.eumc.uniban.listener.BukkitPlayerListener;
import cc.eumc.uniban.listener.PaperListener;
import cc.eumc.uniban.task.LocalBanListRefreshTask;
import cc.eumc.uniban.task.SubscriptionRefreshTask;
import cc.eumc.uniban.task.UpdateCheckTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

public final class UniBanBukkitPlugin extends JavaPlugin {
    static UniBanBukkitPlugin instance;
    BukkitTask Task_LocalBanListRefreshTask;
    BukkitTask Task_SubscriptionRefreshTask;
    UniBanBukkitController controller;
    BukkitConfig bukkitConfig;
    boolean isPaper = false;

    @Override
    public void onEnable() {
        instance = this;

        // Check if running Paper
        try {
            Class.forName("com.destroystokyo.paper.event.server.PaperServerListPingEvent");
            isPaper = true;
        } catch (ClassNotFoundException ignore) { }

        reloadConfig();

        controller = new UniBanBukkitController();

        registerTask();
        registerCommand();

        Bukkit.getPluginManager().registerEvents(new BukkitPlayerListener(this), this);
        if (PluginConfig.EnableBroadcast && PluginConfig.ViaServerListPing_Enabled) {
            if (isPaper) {
                Bukkit.getPluginManager().registerEvents(new PaperListener(this), this);
            } else {
                getLogger().warning("Failed enabling ban-list delivery via server list ping: Paper server required.");
            }
        }

        getLogger().info("UniBan Enabled");
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

    @Override
    public void reloadConfig() {
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            saveDefaultConfig();
        }

        super.reloadConfig();

        bukkitConfig = new BukkitConfig(this);

        showPluginInformation();
    }

    public void showPluginInformation() {
        getLogger().info(String.format(Message.SubscriptionsHeader, BukkitConfig.Subscriptions.size()));
        for (ServerEntry serverEntry : BukkitConfig.Subscriptions.keySet()) {
            getLogger().info("* " + serverEntry.getAddress() + (BukkitConfig.Subscriptions.get(serverEntry.getAddress())!=null?" | "+Message.Encrypted:""));
        }
        getLogger().info(Message.ThirdPartyPluginSupportHeader);
        getLogger().info("* AdvancedBan: " + (ThirdPartySupportConfig.AdvancedBan?Message.PluginEnabled:Message.PluginNotFound));
        //getLogger().info(Message.MessagePrefix + "* BungeeAdminTool: " + (ThirdPartySupportConfig.BungeeAdminTool?Message.PluginEnabled:Message.PluginNotFound));
        //getLogger().info(Message.MessagePrefix + "* BungeeBan: " + (ThirdPartySupportConfig.BungeeBan?Message.PluginEnabled:Message.PluginNotFound));
        getLogger().info("* LiteBans: " + (ThirdPartySupportConfig.LiteBans?Message.PluginEnabled:Message.PluginNotFound));
        getLogger().info("* VanillaList: " + (ThirdPartySupportConfig.VanillaList?Message.PluginEnabled:Message.PluginNotFound));
    }

    public void reloadController() {
        if (controller != null)
            controller.destruct();
        controller = new UniBanBukkitController();
    }

    void registerCommand() {
        getCommand("uniban").setExecutor(new BukkitCommand(this));
    }

    public void registerTask() {
        if (Task_LocalBanListRefreshTask != null)
            Task_LocalBanListRefreshTask.cancel();
        if (PluginConfig.EnableBroadcast)
            Task_LocalBanListRefreshTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new LocalBanListRefreshTask(getController(), false), 1,
                20 * (int) (60 * PluginConfig.LocalBanListRefreshPeriod));

        if (Task_SubscriptionRefreshTask != null)
            Task_SubscriptionRefreshTask.cancel();
        Task_SubscriptionRefreshTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new SubscriptionRefreshTask(getController()), 20,
                20 * (int) (60 * PluginConfig.SubscriptionRefreshPeriod));

        Bukkit.getScheduler().runTaskAsynchronously(this, new UpdateCheckTask(getDescription().getVersion(), 74747));
        // TODO run IdentifySubscriptionTask
    }

    public UniBanBukkitController getController() {
        return controller;
    }

    public static UniBanBukkitPlugin getInstance() {
        return instance;
    }

    public BukkitConfig getBukkitConfig() {
        return bukkitConfig;
    }

    public boolean isPaper() {
        return isPaper;
    }
}
