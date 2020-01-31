package cc.eumc.task;

import cc.eumc.config.PluginConfig;
import cc.eumc.controller.UniBanController;
import cc.eumc.util.Encryption;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SubscriptionRefreshTask implements Runnable {
    final UniBanController controller;
    boolean running = false;
    public SubscriptionRefreshTask(UniBanController instance) {
        this.controller = instance;
    }

    @Override
    public void run() {
        if (running) {
            return;
        }
        running = true;

        int count = 0;
        for (String address : PluginConfig.Subscriptions.keySet()) {
            try {
                String result = getHTML("http://" + address + "/get");
                result = Encryption.decrypt(result, PluginConfig.Subscriptions.get(address));
                if (result == null) {
                    controller.sendWarning("Failed decrypting ban-list from: " + address + ". Is the password correct?");
                    continue;
                }
                else {
                    Type playerListType = new TypeToken<ArrayList<String>>(){}.getType();
                    List<String> banList = new Gson().fromJson(result, playerListType);
                    if (banList == null) {
                        controller.sendWarning(address + " responded with invalid data (length " + result.length() + ")");
                        //plugin.getLogger().warning(result);
                        continue;
                    }
                    if (banList.size() == 0) {
                        continue;
                    }
                    String host = address.substring(0, address.lastIndexOf(":")); // Fix wrong host address
                    for (String uuidStr : banList) {
                        try {
                            // Check if the provided UUID is valid
                            UUID uuid = UUID.fromString(uuidStr);
                            String validationUUID = uuid.toString();
                            if (validationUUID.equals(uuidStr)) {
                                controller.addOnlineBanned(uuid, host);
                                count ++;
                            }
                        } catch(IllegalArgumentException e) {
                            continue;
                        }
                    }
                    controller.purgeOnlineBannedOfHost(host, banList);
                }
            }
            catch (SocketException e) {
                controller.sendWarning("Failed pulling ban-list from: " + address + ". Increasing refreshing interval may help addressing this problem.");
            }
            catch (Exception e) {
                //e.printStackTrace();
                controller.sendWarning("Failed pulling ban-list from: " + address);
            }
        }
        controller.saveBanList();
        controller.sendInfo("Updated " + count + " banned players from other servers.");

        running = false;
    }

    static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }
}
