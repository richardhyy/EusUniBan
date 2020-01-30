package cc.eumc.config;

import cc.eumc.util.Encryption;

import java.security.Key;
import java.util.*;

public abstract class PluginConfig {
    public static boolean EnableBroadcast;
    public static double LocalBanListRefreshPeriod;
    public static double SubscriptionRefreshPeriod;
    public static String Host;
    public static int Port;
    public static int Threads;
    public static Key EncryptionKey;
    public static String ServerID;

    public static boolean EnabledAccessControl;
    public static int MinPeriodPerServer;
    public static boolean BlacklistEnabled;
    public static boolean WhitelistEnabled;
    public static List<String> Blacklist;
    public static List<String> Whitelist;

    public static Map<String, Key> Subscriptions = new HashMap<>();
    public static Map<String, String> SubscriptionServerHostIDMap = new HashMap<>();

    public static List<String> UUIDWhitelist;

    public static String BannedOnlineKickMessage;

    public PluginConfig() {
        EnableBroadcast = configGetBoolean("Settings.Broadcast.Enabled", true);
        if (EnableBroadcast) {
            LocalBanListRefreshPeriod = configGetDouble("Settings.Tasks.LocalBanListRefreshPeriod", 1.0);
            SubscriptionRefreshPeriod = configGetDouble("Settings.Tasks.SubscriptionRefreshPeriod", 10.0);
            Host = configGetString("Settings.Broadcast.Host", "0.0.0.0");
            Port = configGetInt("Settings.Broadcast.Port", 60009);
            Threads = configGetInt("Settings.Broadcast.Threads", 2);
            EncryptionKey = Encryption.getKeyFromString(configGetString("Settings.Broadcast.Password", ""));
        }
        ServerID = configGetString("Settings.Broadcast.ServerID", "");
        if (ServerID.equals("")) {
            ServerID = UUID.randomUUID().toString();
            configSet("Settings.Broadcast.ServerID", ServerID);
            saveConfig();
        }

        if (configIsSection("Subscription")) {
            for (String key : getConfigurationSectionKeys("Subscription")) {
                String host = configGetString("Subscription."+key+".Host", "");
                if (host.equals("")) continue;
                String port = String.valueOf(configGetInt("Subscription."+key+".Port", 60009));
                String password = configGetString("Subscription."+key+".Password", "");
                Key decryptionKey = null;
                if (!password.equals(""))
                    decryptionKey = Encryption.getKeyFromString(password);

                Subscriptions.put(host+":"+port, decryptionKey);

                // TODO run IdentifySubscriptionTask
                //Bukkit.getScheduler().runTaskLater(instance, new IdentifySubscriptionTask(instance), 1);
            }
        }

        EnabledAccessControl = configGetBoolean("Settings.Broadcast.AccessControl.Enabled", true);
        if (EnabledAccessControl) {
            MinPeriodPerServer = (int) (60 * configGetDouble("Settings.Broadcast.AccessControl.MinPeriodPerServer", 1.0));

            BlacklistEnabled = configGetBoolean("Settings.Broadcast.AccessControl.Blacklist.Enabled", false);
            if (BlacklistEnabled) {
                Blacklist = new ArrayList<>(configGetStringList("Settings.Broadcast.AccessControl.Blacklist.IPList"));
            }

            WhitelistEnabled = configGetBoolean("Settings.Broadcast.AccessControl.Whitelist.Enabled", false);
            if (WhitelistEnabled) {
                Whitelist = new ArrayList<>(configGetStringList("Settings.Broadcast.AccessControl.Whitelist.IPList"));
            }
        }

        UUIDWhitelist = configGetStringList("UUIDWhitelist");

        BannedOnlineKickMessage = configGetString("Message.BannedOnlineKickMessage", "§eSorry, you have been banned from another server.").replace("&", "§");
   }

    abstract boolean configGetBoolean(String path, Boolean def);
    abstract String configGetString(String path, String def);
    abstract Double configGetDouble(String path, Double def);
    abstract int configGetInt(String path, int def);
    abstract List<String> configGetStringList(String path);
    abstract boolean configIsSection(String path);
    abstract Set<String> getConfigurationSectionKeys(String path);

    abstract void configSet(String path, Object object);
    abstract void saveConfig();

}
