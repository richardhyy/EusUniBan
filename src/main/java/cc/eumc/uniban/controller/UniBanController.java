package cc.eumc.uniban.controller;

import cc.eumc.uniban.config.Message;
import cc.eumc.uniban.config.PluginConfig;
import cc.eumc.uniban.extension.HttpService;
import cc.eumc.uniban.extension.UniBanExtension;
import cc.eumc.uniban.handler.BanListRequestHandler;
import cc.eumc.uniban.handler.IDRequestHandler;
import cc.eumc.uniban.util.Encryption;
import cc.eumc.uniban.util.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.istack.internal.NotNull;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.cgrotz.kademlia.node.Key;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class UniBanController {
    HttpServer server;
    DeliveryController deliveryController;
    boolean serverStarted;
    List<UniBanExtension> extensions = new ArrayList<>();

    final String banListFilename = "banlist.json";

    String banJson = "";
    Set<UUID> localBanned = new HashSet<>();

    Map<UUID, List<String>> bannedPlayerOnline = new HashMap<>();

    public UniBanController () {
        serverStarted = false;
        loadBanListFromDisk();

        if (PluginConfig.EnableBroadcast) {
            if (!PluginConfig.ActiveMode_Enabled) {
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

            if (PluginConfig.EnableDHT) {
                deliveryController = new DeliveryController(this);
            }
        }

    }

    public void destruct() {
        unloadExtensions();
        if (server != null) {
            server.stop(0);
        }
    }

    public HttpContext createHttpContent(String path, HttpHandler handler) {
        if (server == null) return null;

        return server.createContext(path, handler);
    }

    public boolean removeHttpContent(HttpContext httpContext) {
        if (server == null) return false;

        server.removeContext(httpContext);
        return true;
    }

    public void registerExtension(UniBanExtension extension) {
        extensions.add(extension);
        loadExtension(extension);
    }

    void loadExtension(UniBanExtension extension) {
        try {
            sendInfo("Loading extension: " + extension.getName() + "(" + extension.getVersion() + ")@" + extension.getAuthor());
            extension.onExtensionLoad();

            HttpService httpService;
            if ((httpService = extension.getHttpService()) != null) {
                HttpContext httpContext;
                if ((httpContext = createHttpContent(httpService.path, httpService.handler)) != null) {
                    extension.getHttpService().setContext(httpContext);
                    sendInfo("HTTPContent registered for extension: " + extension.getName());
                }
                else {
                    sendWarning("Failed registering HTTPContent for extension: " + extension.getName());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void unloadExtensions() {
        try {
            extensions.forEach(UniBanExtension::onExtensionUnload);
        } catch (Exception e) {
            e.printStackTrace();
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

            sendInfo(String.format(Message.LoadedFromLocalCache, count));
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
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            fw.write(gson.toJson(banListArray));
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

    public void purgeOnlineBannedOfHost(String address, List<String> banList) {
        //List<String> banList = new ArrayList<String>(Arrays.asList(list));
        for (UUID uuid : bannedPlayerOnline.keySet()) {
            // Fix: the player would not be removed even if he/she is unbanned from all subscribed servers
            if (bannedPlayerOnline.get(uuid).contains(address) && !banList.contains(uuid.toString()) ) {
                removeOnlineBanned(uuid, address);
                break;
            }
        }
    }

    public void removeOnlineBanned(UUID uuid, String fromServer) {
        if (bannedPlayerOnline.containsKey(uuid)) {
            List<String> serverList = new ArrayList<>(bannedPlayerOnline.get(uuid));

            if (serverList.size() == 1 && serverList.get(0).equals(fromServer)) { // The player was only banned from this server
                bannedPlayerOnline.remove(uuid);
            }
            // Fix: the player would not be removed even if he/she is unbanned from all subscribed servers
            else {
                serverList.remove(fromServer);
                bannedPlayerOnline.put(uuid, serverList);
            }
        }
    }

    public Boolean isBannedOnline(UUID uuid) {
        // NOT_TODO Display bannedFrom.
        return bannedPlayerOnline.containsKey(uuid);
    }

    public int getBannedServerAmount(@NotNull UUID uuid) {
        // Fix: Null pointer exception
        List<String> fromServer = bannedPlayerOnline.get(uuid);
        if (fromServer == null)
            return 0;
        else
            return fromServer.size();
    }

    public List<String> getBannedServerList(UUID uuid) {
        return bannedPlayerOnline.get(uuid);
    }

    public void addLocalBan(UUID uuid) {
        localBanned.add(uuid);
        updateLocalBanListCache(localBanned, false);
    }

    public void removeLocalBan(UUID uuid) {
        localBanned.remove(uuid);
        updateLocalBanListCache(localBanned, false);
    }

    public void updateLocalBanListCache(Set<UUID> uuidSet) {
        updateLocalBanListCache(uuidSet, true);
    }

    public void updateLocalBanListCache(Set<UUID> uuidSet, boolean updateBanSetCache) {
        if (localBanned.equals(uuidSet)) return;

        if (updateBanSetCache) localBanned = uuidSet;

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

        sendLocalBanListToDHT();
    }

    public String getBanListJson() {
        return banJson;
    }

    public void sendLocalBanListToDHT() {
        if (!DeliveryController.isReady()) return;

        deliveryController.put(Key.build(""), "{{banList}}" + getBanListJson() + "{{/banList}}");
    }

    public void sendLocalBanListToURL() {
        try {
            HttpRequest.post(new URL(PluginConfig.ActiveMode_PostUrl))
                    .bodyForm(HttpRequest.Form.create().add("list", getBanListJson()).add("secret", PluginConfig.ActiveMode_PostSecret))
                    .execute()
                    .expectResponseCode(200);
        } catch (IOException e) {
            sendWarning("Error posting ban-list to remote url:");
            e.printStackTrace();
        }
    }

    public boolean shouldIgnoreReason(String reason) {
        if (PluginConfig.ExcludeIfReasonContain.size() == 0 || reason == null) {
            return false;
        }

        final String reasonLowerCase = reason.toLowerCase();

        for (String keyword : PluginConfig.ExcludeIfReasonContain) {
            if (reasonLowerCase.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
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

    public abstract void runTaskLater(Runnable task, int delayTick);

}
