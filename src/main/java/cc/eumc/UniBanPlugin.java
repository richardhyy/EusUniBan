package cc.eumc;

import cc.eumc.command.CommandManager;
import cc.eumc.handler.RequestHandler;
import cc.eumc.listener.PlayerListener;
import cc.eumc.task.LocalBanListRefreshTask;
import cc.eumc.task.SubscriptionRefreshTask;
import cc.eumc.util.Encryption;
import com.google.gson.Gson;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;

public final class UniBanPlugin extends JavaPlugin {
    HttpServer server;
    AccessController accessController;
    BukkitTask Task_LocalBanListRefreshTask;
    BukkitTask Task_SubscriptionRefreshTask;

    final String banListFilename = "banlist.yml";

    String banJson = "";

    Map<UUID, List<String>> bannedPlayerOnline = new HashMap<>();

    @Override
    public void onEnable() {
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            saveDefaultConfig();
        }

        reloadConfig();

        loadBanListFromDisk();

        accessController = new AccessController();

        try {
            server = HttpServer.create(new InetSocketAddress(PluginConfig.Host, PluginConfig.Port), 0);
            server.createContext("/get", new RequestHandler(this));
            server.setExecutor(Executors.newFixedThreadPool(PluginConfig.Threads));
            server.start();
            getLogger().info("UniBan broadcast started on " + PluginConfig.Host + ":" + PluginConfig.Port + " (" + PluginConfig.Threads + " Threads)");
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("Failed starting broadcast server");
        }

        registerTask();
        registerCommand();

        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("UniBan Enabled");
    }

    @Override
    public void onDisable() {
        if (Task_LocalBanListRefreshTask != null)
            Task_LocalBanListRefreshTask.cancel();
        if (Task_SubscriptionRefreshTask != null)
            Task_SubscriptionRefreshTask.cancel();
        saveBanList();
        getLogger().info("UniBan Disabled");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        new PluginConfig(this);
    }

    void registerCommand() {
        getCommand("uniban").setExecutor(new CommandManager(this));
    }

    public void registerTask() {
        if (Task_LocalBanListRefreshTask != null)
            Task_LocalBanListRefreshTask.cancel();
        Task_LocalBanListRefreshTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new LocalBanListRefreshTask(this), 1,
                20 * (int) (60 * PluginConfig.LocalBanListRefreshPeriod));
        if (Task_SubscriptionRefreshTask != null)
            Task_SubscriptionRefreshTask.cancel();
        Task_SubscriptionRefreshTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new SubscriptionRefreshTask(this), 20,
                20 * (int) (60 * PluginConfig.SubscriptionRefreshPeriod));
    }

    public void loadBanListFromDisk() {
        File file = new File(getDataFolder(), banListFilename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        FileConfiguration banListConfig = YamlConfiguration.loadConfiguration(file);
        if (banListConfig.isConfigurationSection("UniBanList")) {
            for (String uuidStr : banListConfig.getConfigurationSection("UniBanList").getKeys(false)) {
                try {
                    // Check if the provided UUID is valid
                    UUID uuid = UUID.fromString(uuidStr);
                    String validationUUID = uuid.toString();
                    if (validationUUID.equals(uuidStr)) {
                        if (banListConfig.isConfigurationSection("UniBanList."+uuidStr))
                            for (String host : banListConfig.getConfigurationSection("UniBanList." + uuidStr).getKeys(false)) {
                                addOnlineBanned(uuid, host);
                            }
                    }
                } catch(IllegalArgumentException e) {
                    getLogger().warning("Invalid UUID: " + uuidStr);
                    continue;
                }
            }
        }
    }

    public void saveBanList() {
        File file = new File(getDataFolder(), banListFilename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        FileConfiguration banListConfig = YamlConfiguration.loadConfiguration(file);
        for (UUID uuid : bannedPlayerOnline.keySet()) {
            List<String> hosts = bannedPlayerOnline.get(uuid);
            banListConfig.set("UniBanList."+uuid.toString(), hosts);
        }
        try {
            banListConfig.save(file);
        } catch (IOException e) {
            getLogger().severe("Failed saving " + banListFilename);
        }
    }

    public void addWhitelist(UUID uuid) {
        PluginConfig.UUIDWhitelist.add(uuid.toString());
        List<String> whitelist = getConfig().getStringList("UUIDWhitelist");
        whitelist.add(uuid.toString());
        getConfig().set("UUIDWhitelist", whitelist);
        saveConfig();
    }

    public void removeWhitelist(UUID uuid) {
        PluginConfig.UUIDWhitelist.remove(uuid.toString());
        List<String> whitelist = getConfig().getStringList("UUIDWhitelist");
        whitelist.remove(uuid.toString());
        getConfig().set("UUIDWhitelist", whitelist);
        saveConfig();
    }

    public void addOnlineBanned(UUID uuid, String fromServer) {
        if (bannedPlayerOnline.containsKey(uuid)) {
            List<String> serverList = new ArrayList<>(bannedPlayerOnline.get(uuid));
            serverList.add(fromServer);
            bannedPlayerOnline.put(uuid, serverList);
        }
        else {
            bannedPlayerOnline.put(uuid, Collections.singletonList(fromServer));
        }
    }

    public void purgeOnlineBannedOfHost(String host, List<String> banList) {
        //List<String> banList = new ArrayList<String>(Arrays.asList(list));
        for (UUID uuid : bannedPlayerOnline.keySet()) {
            if (!bannedPlayerOnline.get(uuid).contains(host) && !banList.contains(uuid.toString()) ) {
                removeOnlineBanned(uuid, host);
            }
        }
    }

    public void removeOnlineBanned(UUID uuid, String fromServer) {
        if (bannedPlayerOnline.containsKey(uuid)) {
            List<String> serverList = new ArrayList<>(bannedPlayerOnline.get(uuid));

            if (serverList.size() == 1 && serverList.get(0).equals(fromServer)) // The player was only banned from this server
                bannedPlayerOnline.remove(uuid);

            serverList.remove(fromServer);
            bannedPlayerOnline.put(uuid, serverList);
        }
    }

    public Boolean isBannedOnline(@Nullable Player player) {
        if (player == null) return false;
        return isBannedOnline(player.getUniqueId());
    }

    public Boolean isBannedOnline(UUID uuid) {
        // NOT_TODO Display bannedFrom.
        return bannedPlayerOnline.containsKey(uuid);
    }

    public Integer getBannedServerAmount(@NotNull Player player) {
        return getBannedServerAmount(player.getUniqueId());
    }

    public Integer getBannedServerAmount(@NotNull UUID uuid) {
        return bannedPlayerOnline.get(uuid).size();
    }

    public void updateLocalBanListCache(Set<UUID> uuidSet) {
        List<String> uuidStringList = new ArrayList<>();
        for (UUID uuid : uuidSet) {
            uuidStringList.add(uuid.toString());
        }
        String json = Encryption.encrypt(new Gson().toJson(uuidStringList), PluginConfig.encryptionKey);
        if (json == null) {
            getLogger().severe("§cFailed encrypting ban-list.");
            json = "";
        }
        this.banJson = json;
    }

    public String getBanListJson() {
        return banJson;
    }

    public AccessController getAccessController() {
        return accessController;
    }
}
