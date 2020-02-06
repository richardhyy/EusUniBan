package cc.eumc.config;

import cc.eumc.util.Encryption;

import java.security.Key;
import java.util.*;

public abstract class PluginConfig {
    public static boolean LiteBans;
    public static boolean AdvancedBan;

    public final static int PluginConfigVersion = 2;

    public static int ConfigVersion;
    public static boolean EnableBroadcast;
    public static double LocalBanListRefreshPeriod;
    public static double SubscriptionRefreshPeriod;
    public static String Host;
    public static int Port;
    public static int Threads;
    public static String Password;
    public static Key EncryptionKey;
    public static String ServerID;

    public static boolean EnabledAccessControl;
    public static int MinPeriodPerServer;
    public static boolean BlacklistEnabled;
    public static boolean WhitelistEnabled;
    public static List<String> Blacklist;
    public static List<String> Whitelist;

    public static Map<ServerEntry, Key> Subscriptions = new HashMap<>(); // Address - Key
    //public static Map<String, String> SubscriptionServerHostIDMap = new HashMap<>();
    public static int TemporarilyPauseUpdateThreshold;

    public static List<String> UUIDWhitelist;

    public static int WarnThreshold;
    public static int BanThreshold;

    public static String WarningMessage;
    public static String BannedOnlineKickMessage;

    public PluginConfig() {
        ConfigVersion =  configGetInt("ConfigVersion", -1);
        EnableBroadcast = configGetBoolean("Settings.Broadcast.Enabled", true);
        // Fix: SubscriptionRefreshPeriod will not be loaded when broadcast is disabled
        SubscriptionRefreshPeriod = configGetDouble("Settings.Tasks.SubscriptionRefreshPeriod", 10.0);
        if (EnableBroadcast) {
            LocalBanListRefreshPeriod = configGetDouble("Settings.Tasks.LocalBanListRefreshPeriod", 1.0);
            Host = configGetString("Settings.Broadcast.Host", "0.0.0.0");
            Port = configGetInt("Settings.Broadcast.Port", 60009);
            Threads = configGetInt("Settings.Broadcast.Threads", 2);
            Password = configGetString("Settings.Broadcast.Password", "");
            EncryptionKey = Encryption.getKeyFromString(Password);
        }
        ServerID = configGetString("Settings.Broadcast.ServerID", "");
        if (ServerID.equals("")) {
            ServerID = UUID.randomUUID().toString();
            configSet("Settings.Broadcast.ServerID", ServerID);
            saveConfig();
        }

        // Fix: Subscriptions will not be cleaned when reloading
        Subscriptions = new HashMap<>();
        if (configIsSection("Subscription")) {
            for (String key : getConfigurationSectionKeys("Subscription")) {
                String host = configGetString("Subscription."+key+".Host", "");
                if (host.equals("")) continue;
                int port = configGetInt("Subscription."+key+".Port", 60009);
                String password = configGetString("Subscription."+key+".Password", "");
                Key decryptionKey = null;
                if (!password.equals(""))
                    decryptionKey = Encryption.getKeyFromString(password);

                Subscriptions.put(new ServerEntry(host, port), decryptionKey);

                // TODO run IdentifySubscriptionTask
                //Bukkit.getScheduler().runTaskLater(instance, new IdentifySubscriptionTask(instance), 1);
            }
        }

        //TemporarilyPauseUpdateThreshold = configGetInt("Settings.TemporarilyPauseUpdateThreshold", 10);

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

        WarnThreshold = configGetInt("Settings.WarnThreshold", 1);
        BanThreshold = configGetInt("Settings.BanThreshold", 1);

        WarningMessage = configGetString("Message.WarningMessage", "&bUniban &3&l> &eWarning: Player {player}({uuid}) has been banned from another {number} server(s).").replace("&", "§");
        BannedOnlineKickMessage = configGetString("Message.BannedOnlineKickMessage", "§eSorry, you have been banned from another server.").replace("&", "§");
    }


    public void addSubscription(ServerEntry serverEntry, String password, boolean save) {
        Subscriptions.put(serverEntry, Encryption.getKeyFromString(password));

        if (save) {
            String key = String.valueOf(System.currentTimeMillis());
            configSet("Subscription." + key + ".Host", serverEntry.host);
            configSet("Subscription." + key + ".Port", serverEntry.port);
            configSet("Subscription." + key + ".Password", password);
            saveConfig();
        }
    }

    public boolean removeSubscription(String address, boolean save) {
        ServerEntry serverEntry = new ServerEntry(address);
        boolean isRemoved = false;

        for (ServerEntry listedServerEntry : Subscriptions.keySet()) {
            if (listedServerEntry.host.equals(serverEntry.host)
                    && (serverEntry.port == -1 || (serverEntry.port == listedServerEntry.port))) {
                Subscriptions.remove(listedServerEntry);
                isRemoved = true;
                break;
            }
        }

        if (save) {
            boolean isSaved = false;
            for (String key : getConfigurationSectionKeys("Subscription")) {
                if (configGetString("Subscription."+key+".Host", "").equals(serverEntry.host)
                && configGetInt("Subscription."+key+".Port", 0) == (serverEntry.port)) {
                    configSet("Subscription." + key, null);
                    saveConfig();
                    isSaved = true;
                    break;
                }
            }
            return isSaved;
        }

        return isRemoved;
    }

    public String[] getSubscriptions() {
        // TODO Performance optimization
        List<String> addressList = new ArrayList<>();
        for (ServerEntry serverEntry : PluginConfig.Subscriptions.keySet()) {
            addressList.add(serverEntry.host + ":" + serverEntry.port);
        }
        return addressList.toArray(new String[0]);
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
