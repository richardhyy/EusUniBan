package cc.eumc;

import cc.eumc.command.BukkitCommand;
import cc.eumc.config.BukkitConfig;
import cc.eumc.config.PluginConfig;
import cc.eumc.config.SubscriptionGroupEntry;
import cc.eumc.config.SubscriptionServerEntry;
import cc.eumc.controller.UniBanBukkitController;
import cc.eumc.listener.BukkitPlayerListener;
import cc.eumc.task.LocalBanListRefreshTask;
import cc.eumc.task.SubscriptionRefreshTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

public final class UniBanBukkitPlugin extends JavaPlugin {
    static UniBanBukkitPlugin instance;
    BukkitTask Task_LocalBanListRefreshTask;
    BukkitTask Task_SubscriptionRefreshTask;
    UniBanBukkitController controller;

    @Override
    public void onEnable() {
        instance = this;

        reloadConfig();

        controller = new UniBanBukkitController();

        registerTask();
        registerCommand();

        Bukkit.getPluginManager().registerEvents(new BukkitPlayerListener(this), this);

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

        new BukkitConfig(this);

        showSubscriptionInformation();
    }

    public void showSubscriptionInformation() {
        getLogger().info("Groups [" + BukkitConfig.SubscriptionGroups.size() + "] -----");
        for (String groupName : BukkitConfig.SubscriptionGroups.keySet()) {
            SubscriptionGroupEntry groupEntry = BukkitConfig.SubscriptionGroups.get(groupName);
            getLogger().info("* " + groupName + " | WarnThreshold: " + groupEntry.WarnThreshold + " | BanThreshold: " + groupEntry.BanThreshold + (groupEntry.IsDefault?" | Default":""));
        }
        getLogger().info("Subscriptions [" + BukkitConfig.Subscriptions.size() + "] -----");
        for (String address : BukkitConfig.Subscriptions.keySet()) {
            SubscriptionServerEntry serverEntry = BukkitConfig.Subscriptions.get(address);
            getLogger().info("* " + address + " | Group: " + serverEntry.group.groupName + (serverEntry.key!=null?" | Encrypted":""));
        }
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
            Task_LocalBanListRefreshTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new LocalBanListRefreshTask(getController()), 1,
                20 * (int) (60 * PluginConfig.LocalBanListRefreshPeriod));

        if (Task_SubscriptionRefreshTask != null)
            Task_SubscriptionRefreshTask.cancel();
        Task_SubscriptionRefreshTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new SubscriptionRefreshTask(getController()), 20,
                20 * (int) (60 * PluginConfig.SubscriptionRefreshPeriod));

        // TODO run IdentifySubscriptionTask
    }

    public UniBanBukkitController getController() {
        return controller;
    }

    public static UniBanBukkitPlugin getInstance() {
        return instance;
    }
}
