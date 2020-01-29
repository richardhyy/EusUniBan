package cc.eumc.controller;

import cc.eumc.config.PluginConfig;
import cc.eumc.handler.BanListRequestHandler;
import cc.eumc.handler.IDRequestHandler;
import cc.eumc.util.Encryption;
import com.google.gson.Gson;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;

public abstract class UniBanController {
    HttpServer server;
    boolean serverStarted = false;

    final String banListFilename = "banlist.yml";

    String banJson = "";

    Map<UUID, List<String>> bannedPlayerOnline = new HashMap<>();

    public UniBanController () {
        loadBanListFromDisk();

        if (PluginConfig.EnableBroadcast) {
            try {
                server = HttpServer.create(new InetSocketAddress(PluginConfig.Host, PluginConfig.Port), 0);
                server.createContext("/get", new BanListRequestHandler(this));
                // experiential
                server.createContext("/identify", new IDRequestHandler(this));
                server.setExecutor(Executors.newFixedThreadPool(PluginConfig.Threads));
                server.start();
                serverStarted = true;
            } catch (IOException e) {
                e.printStackTrace();
                serverStarted = false;
            }
        }
    }

    public void destruct() {
        if (server != null) {
            server.stop(0);
        }
    }

    public abstract File getDataFolder();
    public abstract void saveConfig();

    public void loadBanListFromDisk() {
        File file = new File(getDataFolder(), banListFilename);
        if (!file.exists()) {
            try {
                file.createNewFile();
                return;
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
                        if (banListConfig.isConfigurationSection("UniBanList." + uuidStr))
                            for (String host : banListConfig.getConfigurationSection("UniBanList." + uuidStr).getKeys(false)) {
                                addOnlineBanned(uuid, host);
                            }
                    }
                } catch (IllegalArgumentException e) {
                    sendWarning("Invalid UUID: " + uuidStr);
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
            sendSevere("Failed saving " + banListFilename);
        }
    }

    public void addWhitelist(UUID uuid) {
        PluginConfig.UUIDWhitelist.add(uuid.toString());
        List<String> whitelist = getStringList("UUIDWhitelist");
        whitelist.add(uuid.toString());
        configSet("UUIDWhitelist", whitelist);
        saveConfig();
    }

    public void removeWhitelist(UUID uuid) {
        PluginConfig.UUIDWhitelist.remove(uuid.toString());
        List<String> whitelist = getStringList("UUIDWhitelist");
        whitelist.remove(uuid.toString());
        configSet("UUIDWhitelist", whitelist);
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
        String json = Encryption.encrypt(new Gson().toJson(uuidStringList), PluginConfig.EncryptionKey);
        if (json == null) {
            sendSevere("§cFailed encrypting ban-list.");
            json = "";
        }
        this.banJson = json;
    }

    public String getBanListJson() {
        return banJson;
    }

    public abstract void sendInfo(String message);
    public abstract void sendWarning(String message);
    public abstract void sendSevere(String message);

    // TODO complete abstracting configuration methods
    abstract boolean configGetBoolean(String path, Boolean def);
    abstract List<String> getStringList(String path);
    abstract void configSet(String path, Object object);
}
