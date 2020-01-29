package cc.eumc;

import cc.eumc.command.BukkitCommand;
import cc.eumc.config.PluginConfig;
import cc.eumc.controller.UniBanBukkitController;
import cc.eumc.listener.BukkitPlayerListener;
import cc.eumc.task.LocalBanListRefreshTask;
import cc.eumc.task.SubscriptionRefreshTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

public final class UniBanBukkitPlugin extends JavaPlugin {
    BukkitTask Task_LocalBanListRefreshTask;
    BukkitTask Task_SubscriptionRefreshTask;
    UniBanBukkitController controller;

    @Override
    public void onEnable() {
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            saveDefaultConfig();
        }

        reloadConfig();

        controller = new UniBanBukkitController(this);

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
        super.reloadConfig();

        new PluginConfig(this);

    }

    public void reloadController() {
        if (controller != null)
            controller.destruct();
        controller = new UniBanBukkitController(this);
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
}
