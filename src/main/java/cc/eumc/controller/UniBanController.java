package cc.eumc.controller;

import cc.eumc.config.PluginConfig;
import cc.eumc.handler.BanListRequestHandler;
import cc.eumc.handler.IDRequestHandler;
import cc.eumc.util.Encryption;
import com.google.gson.Gson;
import com.sun.istack.internal.NotNull;
import com.sun.net.httpserver.HttpServer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class UniBanController {
    HttpServer server;
    boolean serverStarted;

    final String banListFilename = "banlist.json";

    String banJson = "";

    Map<UUID, List<String>> bannedPlayerOnline = new HashMap<>();

    public UniBanController () {
        serverStarted = false;
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

        JSONParser jsonParser = new JSONParser();

        try {
            FileReader reader = new FileReader(file);
            Object obj = null;
            obj = jsonParser.parse(reader);
            JSONArray uuidhostArray = (JSONArray) obj;

            //Map<String, List<String>> map = new HashMap<>();

            AtomicInteger count = new AtomicInteger();

            uuidhostArray.forEach( object -> {
                JSONObject playerObject = (JSONObject) object;

                String uuidStr = playerObject.get("uuid").toString();
                UUID uuid = UUID.fromString(uuidStr);
                String validationUUID = uuid.toString();
                if (validationUUID.equals(uuidStr)) {
                    JSONArray hostArray = (JSONArray) playerObject.get("server");
                    List<String> hostList = new ArrayList<>();
                    for (int i=0; i<hostArray.size(); i++) {
                        addOnlineBanned(uuid, hostArray.get(i).toString());
                    }
                    count.getAndIncrement();
                }
                else {
                    sendSevere("Invalid UUID: " + uuidStr);
                }

            });

            /*
            uuidListArray.forEach( uuidhostObject -> {
                JSONObject object = (JSONObject) uuidListArray.

                String uuidStr = .toString();
                UUID uuid = UUID.fromString(uuidStr);
                String validationUUID = uuid.toString();
                if (validationUUID.equals(uuidStr)) {
                    JSONObject hostObject = (JSONObject) (uuidhostObject);
                    for (Object key : hostObject.keySet()) {
                        String host = ((JSONObject)key).toString();
                        addOnlineBanned(uuid, host);
                        count.getAndIncrement();
                    }
                }
                else {
                    sendSevere("Invalid UUID: " + uuidStr);
                }
            });*/

            sendInfo("Loaded " + count + " banned players from ban-list cache.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        /*
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
        }*/
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

        JSONArray banListArray = new JSONArray();
        for (UUID uuid : bannedPlayerOnline.keySet()) {
            JSONObject playerObject = new JSONObject();
            JSONArray hostArray = new JSONArray();

            playerObject.put("uuid", uuid.toString());
            hostArray.addAll(bannedPlayerOnline.get(uuid));
            playerObject.put("server", hostArray);

            banListArray.add(playerObject);
        }

        try (FileWriter fw = new FileWriter(file)) {
            fw.write(banListArray.toJSONString());
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
            sendSevere("Failed saving ban-list to " + banListFilename);
        }

        /*
        FileConfiguration banListConfig = YamlConfiguration.loadConfiguration(file);
        for (UUID uuid : bannedPlayerOnline.keySet()) {
            List<String> hosts = bannedPlayerOnline.get(uuid);
            banListConfig.set("UniBanList."+uuid.toString(), hosts);
        }
        try {
            banListConfig.save(file);
        } catch (IOException e) {
            sendSevere("Failed saving " + banListFilename);
        }*/
    }

    public void addWhitelist(UUID uuid) {
        PluginConfig.UUIDWhitelist.add(uuid.toString());
        List<String> whitelist = configGetStringList("UUIDWhitelist");
        whitelist.add(uuid.toString());
        configSet("UUIDWhitelist", whitelist);
        saveConfig();
    }

    public void removeWhitelist(UUID uuid) {
        PluginConfig.UUIDWhitelist.remove(uuid.toString());
        List<String> whitelist = configGetStringList("UUIDWhitelist");
        whitelist.remove(uuid.toString());
        configSet("UUIDWhitelist", whitelist);
        saveConfig();
    }

    public void addOnlineBanned(UUID uuid, String fromServer) {
        if (bannedPlayerOnline.containsKey(uuid)) {
            List<String> serverList = new ArrayList<>(bannedPlayerOnline.get(uuid));
            if (!serverList.contains(fromServer)) { // Fix duplication
                serverList.add(fromServer);
                bannedPlayerOnline.put(uuid, serverList);
            }
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

    public Boolean isBannedOnline(UUID uuid) {
        // NOT_TODO Display bannedFrom.
        return bannedPlayerOnline.containsKey(uuid);
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

    public abstract boolean configGetBoolean(String path, Boolean def);
    public abstract String configGetString(String path, String def);
    public abstract Double configGetDouble(String path, Double def);
    public abstract int configGetInt(String path, int def);
    public abstract List<String> configGetStringList(String path);
    public abstract boolean configIsSection(String path);
    public abstract Set<String> getConfigurationSectionKeys(String path);

    public abstract void configSet(String path, Object object);

}
