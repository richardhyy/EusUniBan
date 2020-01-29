package cc.eumc.config;

import cc.eumc.controller.UniBanController;
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

    public PluginConfig(UniBanController instance) {
        EnableBroadcast = instance.getConfig().getBoolean("Settings.Broadcast.Enabled", true);
        if (EnableBroadcast) {
            LocalBanListRefreshPeriod = instance.getConfig().getDouble("Settings.Tasks.LocalBanListRefreshPeriod", 1);
            SubscriptionRefreshPeriod = instance.getConfig().getDouble("Settings.Tasks.SubscriptionRefreshPeriod", 10);
            Host = instance.getConfig().getString("Settings.Broadcast.Host", "0.0.0.0");
            Port = instance.getConfig().getInt("Settings.Broadcast.Port", 60009);
            Threads = instance.getConfig().getInt("Settings.Broadcast.Threads", 2);
            EncryptionKey = Encryption.getKeyFromString(instance.getConfig().getString("Settings.Broadcast.Password", ""));
        }
        ServerID = instance.getConfig().getString("Settings.Broadcast.ServerID", "");
        if (ServerID.equals("")) {
            ServerID = UUID.randomUUID().toString();
            instance.getConfig().set("Settings.Broadcast.ServerID", ServerID);
            instance.saveConfig();
        }

        if (instance.getConfig().isConfigurationSection("Subscription")) {
            for (String key : instance.getConfig().getConfigurationSection("Subscription").getKeys(false)) {
                String host = instance.getConfig().getString("Subscription."+key+".Host", "");
                if (host.equals("")) continue;
                String port = String.valueOf(instance.getConfig().getInt("Subscription."+key+".Port", 60009));
                String password = instance.getConfig().getString("Subscription."+key+".Password", "");
                Key decryptionKey = null;
                if (!password.equals(""))
                    decryptionKey = Encryption.getKeyFromString(password);

                Subscriptions.put(host+":"+port, decryptionKey);

                // TODO run IdentifySubscriptionTask
                //Bukkit.getScheduler().runTaskLater(instance, new IdentifySubscriptionTask(instance), 1);
            }
        }

        EnabledAccessControl = instance.getConfig().getBoolean("Settings.Broadcast.AccessControl.Enabled", true);
        if (EnabledAccessControl) {
            MinPeriodPerServer = (int) (60 * instance.getConfig().getDouble("Settings.Broadcast.AccessControl.MinPeriodPerServer", 1.0));

            BlacklistEnabled = instance.getConfig().getBoolean("Settings.Broadcast.AccessControl.Blacklist.Enabled", false);
            if (BlacklistEnabled) {
                Blacklist = new ArrayList<>(instance.getConfig().getStringList("Settings.Broadcast.AccessControl.Blacklist.IPList"));
            }

            WhitelistEnabled = instance.getConfig().getBoolean("Settings.Broadcast.AccessControl.Whitelist.Enabled", false);
            if (WhitelistEnabled) {
                Whitelist = new ArrayList<>(instance.getConfig().getStringList("Settings.Broadcast.AccessControl.Whitelist.IPList"));
            }
        }

        UUIDWhitelist = instance.getConfig().getStringList("UUIDWhitelist");

        BannedOnlineKickMessage = instance.getConfig().getString("Message.BannedOnlineKickMessage", "§eSorry, you have been banned from another server.").replace("&", "§");
   }
}
