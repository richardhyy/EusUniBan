package cc.eumc.task;

import cc.eumc.config.PluginConfig;
import cc.eumc.controller.UniBanController;
import cc.eumc.util.Encryption;
import com.google.gson.Gson;
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
            String host = address.substring(0, address.lastIndexOf(":")); // Fix wrong host address
            String port = address.substring(address.lastIndexOf(":")+1, address.length());

            try {
                String result = "";

                switch (port) {
                    case "80":
                        result = httpGet("http://" + host + "/get");
                        break;
                    case "443":
                        result = httpsGet("https://" + host + "/get");
                        break;
                    default:
                        result = httpGet(address + "/get");
                }

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
