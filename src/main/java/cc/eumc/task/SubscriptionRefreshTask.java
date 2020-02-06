package cc.eumc.task;

import cc.eumc.config.PluginConfig;
import cc.eumc.config.ServerEntry;
import cc.eumc.controller.UniBanController;
import cc.eumc.util.Encryption;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
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
    //Map<String, Integer> lastUpdateCountMap = new HashMap<>();

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
        for (ServerEntry serverEntry : PluginConfig.Subscriptions.keySet()) {
            /*if (!lastUpdateCountMap.containsKey(serverEntry.getAddress())) {
                // Mark as pending (update count check will not be performed this time)
                lastUpdateCountMap.put(serverEntry.getAddress(), -1);
            }*/
            String host = serverEntry.host;
            int port = serverEntry.port;

            try {
                String result = "";

                switch (port) {
                    case 80:
                        result = httpGet("http://" + host + "/get");
                        break;
                    case 443:
                        result = httpsGet("https://" + host + "/get");
                        break;
                    default:
                        result = httpGet("http://" + serverEntry.getAddress() + "/get");
                }

                result = Encryption.decrypt(result, PluginConfig.Subscriptions.get(serverEntry));
                if (result == null) {
                    controller.sendWarning("Failed decrypting ban-list from: " + serverEntry.getAddress() + ". Is our password correct?");
                    continue;
                }
                else {
                    Type playerListType = new TypeToken<ArrayList<String>>(){}.getType();
                    try {
                        List<String> banList = new Gson().fromJson(result, playerListType);
                        if (banList == null) {
                            controller.sendWarning(serverEntry.getAddress() + " responded with invalid data (length " + result.length() + ")");
                            //plugin.getLogger().warning(result);
                            continue;
                        }

                        /*
                        // Update count check
                        if (lastUpdateCountMap.get(serverEntry.getAddress()) == -1) {
                            lastUpdateCountMap.put(serverEntry.getAddress(), banList.size());
                        }
                        else if (Math.abs(lastUpdateCountMap.get(serverEntry.getAddress()) - banList.size()) >= PluginConfig.TemporarilyPauseUpdateThreshold) {
                            lastUpdateCountMap.put(serverEntry.getAddress(), -1);
                        }
                        else {
                            lastUpdateCountMap.put(serverEntry.getAddress(), banList.size());
                        }
                         */

                        if (banList.size() == 0) {
                            continue;
                        }
                        for (String uuidStr : banList) {
                            try {
                                // Check if the provided UUID is valid
                                UUID uuid = UUID.fromString(uuidStr);
                                String validationUUID = uuid.toString();
                                if (validationUUID.equals(uuidStr)) {
                                    controller.addOnlineBanned(uuid, serverEntry.getAddress());
                                    count++;
                                }
                            } catch (IllegalArgumentException e) {
                                continue;
                            }
                        }
                        controller.purgeOnlineBannedOfHost(serverEntry.getAddress(), banList);
                    }
                    // Fix: Misleading message when failed resolving ban-list caused by wrong password
                    catch (JsonSyntaxException e) {
                        controller.sendWarning("Failed resolving ban-list from: " + serverEntry.getAddress() + ". Is our password correct?");
                        continue;
                    }
                }
            }
            catch (SocketException e) {
                controller.sendWarning("Failed pulling ban-list from: " + host+":"+port + ". Increasing refreshing interval may help addressing this problem.");
            }
            catch (Exception e) {
                e.printStackTrace();
                controller.sendWarning("Failed pulling ban-list from: " + host+":"+port);
            }
        }
        controller.saveBanList();
        controller.sendInfo("Updated " + count + " banned players from other servers.");

        running = false;
    }

    static String httpGet(String urlToRead) throws Exception {
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

    static String httpsGet(String urlTORead) throws Exception {
        URL myUrl = new URL(urlTORead);
        HttpsURLConnection conn = (HttpsURLConnection)myUrl.openConnection();
        InputStream is = conn.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        String inputLine;
        String result = "";

        while ((inputLine = br.readLine()) != null) {
            result = result + "\n" + inputLine;
        }

        br.close();
        return result;
    }

}
