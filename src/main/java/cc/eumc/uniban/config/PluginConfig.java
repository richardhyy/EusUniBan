package cc.eumc.uniban.config;

import cc.eumc.uniban.util.Encryption;

import java.security.Key;
import java.util.*;

public abstract class PluginConfig {
    public static boolean LiteBans;
    public static boolean AdvancedBan;

    public final static int PluginConfigVersion = 6;

    public static boolean BroadcastWarning;
    public static int ConfigVersion;
    public static boolean EnableBroadcast;
    public static boolean EnableDHT;
    public static double LocalBanListRefreshPeriod;
    public static double SubscriptionRefreshPeriod;
    public static int SubscriptionGetConnectTimeout;
    public static int SubscriptionGetReadTimeout;
    public static String Host;
    public static int Port;
    public static int Threads;
    public static String Password;
    public static Key EncryptionKey;
    public static String NodeID;
    public static boolean ActiveMode_Enabled;
    public static String ActiveMode_PostUrl;
    public static String ActiveMode_PostSecret;

    public static boolean EnabledAccessControl;
    public static int MinPeriodPerServer;
    // TODO Change to `Blocklist` & `Allowlist`
    public static boolean BlacklistEnabled;
    public static boolean WhitelistEnabled;
    public static List<String> Blacklist;
    public static List<String> Whitelist;

    public static Map<ServerEntry, Key> Subscriptions = new HashMap<>(); // Address - Key
    //public static Map<String, String> SubscriptionServerHostIDMap = new HashMap<>();
    public static int TemporarilyPauseUpdateThreshold;

    public static List<String> UUIDWhitelist;
    public static List<String> ExcludeIfReasonContain;

    public static int WarnThreshold;
    public static int BanThreshold;
    public static boolean IgnoreOP;

    public PluginConfig() {
        ConfigVersion = configGetInt("ConfigVersion", -1);
        EnableBroadcast = configGetBoolean("Settings.Broadcast.Enabled", true);
        // Fix: SubscriptionRefreshPeriod will not be loaded when broadcast is disabled
        SubscriptionRefreshPeriod = configGetDouble("Settings.Tasks.SubscriptionRefreshPeriod", 10.0);
        SubscriptionGetConnectTimeout = configGetInt("Settings.Tasks.SubscriptionGetConnectTimeout", 5000);
        SubscriptionGetReadTimeout = configGetInt("Settings.Tasks.SubscriptionGetReadTimeout", 5000);
        if (EnableBroadcast) {
            EnableDHT = configGetBoolean("Settings.Broadcast.EnableDHT", false);
            LocalBanListRefreshPeriod = configGetDouble("Settings.Tasks.LocalBanListRefreshPeriod", 1.0);

            ActiveMode_Enabled = configGetBoolean("Settings.Broadcast.ActiveMode.Enabled", false);
            if (ActiveMode_Enabled) {
                ActiveMode_PostUrl = configGetString("Settings.Broadcast.ActiveMode.PostUrl", "");
                ActiveMode_PostSecret = configGetString("Settings.Broadcast.ActiveMode.PostSecret", "");
            }
            else {
                Host = configGetString("Settings.Broadcast.Host", "0.0.0.0");
                Port = configGetInt("Settings.Broadcast.Port", 60009);
                Threads = configGetInt("Settings.Broadcast.Threads", 2);
                Password = configGetString("Settings.Broadcast.Password", "");
                EncryptionKey = Encryption.getKeyFromString(Password);
            }

            ExcludeIfReasonContain = new ArrayList<>(configGetStringList("Settings.Broadcast.ExcludeIfReasonContain"));
        }
        NodeID = configGetString("Settings.Broadcast.NodeID", "");
        if (NodeID.equals("")) {
            NodeID = de.cgrotz.kademlia.node.Key.random().toString();
            configSet("Settings.Broadcast.NodeID", NodeID);
            saveConfig();
        }

        // Fix: Subscriptions will not be cleaned when reloading
        Subscriptions = new HashMap<>();
        if (configIsSection("Subscription")) {
            for (String key : getConfigurationSectionKeys("Subscription")) {
                String host = configGetString("Subscription." + key + ".Host", "");
                int port;
                if (host.equals("")) {
                    if (!(host = configGetString("Subscription." + key + ".URL", "")).equals("")) {
                        if (host.startsWith("https://")) {
                            port = 443;
                            host = host.replace("https://", "");
                        } else {
                            port = 80;
                            host = host.replace("http://", "");
                        }
                    } else {
                        continue;
                    }
                } else {
                    port = configGetInt("Subscription." + key + ".Port", 60009);
                }
                String password = configGetString("Subscription." + key + ".Password", "");
                Key decryptionKey = null;
                if (!password.equals("")) {
                    decryptionKey = Encryption.getKeyFromString(password);
                }

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

        BroadcastWarning = configGetBoolean("Settings.BroadcastWarning", false);
        WarnThreshold = configGetInt("Settings.WarnThreshold", 1);
        BanThreshold = configGetInt("Settings.BanThreshold", 1);
        IgnoreOP = configGetBoolean("Settings.IgnoreOP", true);

        // Resolve #2
        Message.WarningMessage = Message.replace(configGetString("Message.WarningMessage", "&bUniban &3&l> &eWarning: Player {player}({uuid}) has been banned from another {number} server(s)."));
        Message.BannedOnlineKickMessage = Message.replace(configGetString("Message.BannedOnlineKickMessage", "§eSorry, you have been banned from another server."));
        Message.IgnoredOP = Message.replace(configGetString("Message.IgnoredOP", "Ignored OP: %s"));
        Message.MessagePrefix = Message.replace(configGetString("Message.MessagePrefix", "UniBan &3> &r"));
        Message.Error = Message.replace(configGetString("Message.Error", "Error: %s"));
        Message.Reloaded = Message.replace(configGetString("Message.Reloaded", "Reloaded."));
        Message.PlayerNotExist = Message.replace(configGetString("Message.PlayerNotExist", "Player %s does not exist."));
        Message.PlayerState = Message.replace(configGetString("Message.PlayerState", "Player %s state: %s"));
        Message.PlayerBanned = Message.replace(configGetString("Message.PlayerBanned", "&cBanned by at least 1 server"));
        Message.PlayerNormal = Message.replace(configGetString("Message.PlayerNormal", "&anormal"));
        Message.InvalidSubscriptionKey = Message.replace(configGetString("Message.InvalidSubscriptionKey", "&eInvalid subscription key"));
        Message.SubscriptionKeyAdded = Message.replace(configGetString("Message.SubscriptionKeyAdded", "Successfully added %s to your subscription list."));
        Message.YourSubscriptionKey = Message.replace(configGetString("Message.YourSubscriptionKey", "Here's the sharing link of your server's Subscription Key which contains your address and connection password:"));
        Message.SubscriptionKeyLink = Message.replace(configGetString("Message.SubscriptionKeyLink", "https://uniban.eumc.cc/share.php?key=%s"));
        Message.SubscriptionExempted = Message.replace(configGetString("Message.SubscriptionExempted", "Successfully exempted server %s from subscription list temporarily."));
        Message.FailedExempting = Message.replace(configGetString("Message.FailedExempting", "Failed exempting %s. Does that subscription exist?"));
        Message.WhitelistAdded = Message.replace(configGetString("Message.WhitelistAdded", "Player %s has been added to whitelist"));
        Message.WhitelistRemoved = Message.replace(configGetString("Message.WhitelistRemoved", "Player %s has been removed from whitelist"));
        Message.SubscriptionsHeader = Message.replace(configGetString("Message.SubscriptionsHeader", "Subscriptions [%s] -----"));
        Message.ThirdPartyPluginSupportHeader = Message.replace(configGetString("Message.ThirdPartyPluginSupportHeader", "Third-party Banning Plugin Support -----"));
        Message.Encrypted = Message.replace(configGetString("Message.Encrypted", "Encrypted"));
        Message.PluginEnabled = Message.replace(configGetString("Message.PluginEnabled", "&lEnabled"));
        Message.PluginNotFound = Message.replace(configGetString("Message.PluginNotFound", "&oNot Found"));
        Message.Encrypted = Message.replace(configGetString("Message.Encrypted", "Encrypted"));
        Message.BroadcastActiveModeEnabled = Message.replace(configGetString("Message.BroadcastActiveModeEnabled", "UniBan broadcast service is running under active mode."));
        Message.BroadcastStarted = Message.replace(configGetString("Message.BroadcastStarted", "UniBan broadcast started on %s:%s (%s Threads)"));
        Message.BroadcastFailed = Message.replace(configGetString("Message.BroadcastFailed", "Failed starting broadcast server"));
        Message.UpToDate = Message.replace(configGetString("Message.UpToDate", "You are up-to-date."));
        Message.NewVersionAvailable = Message.replace(configGetString("Message.NewVersionAvailable", "There is a newer version %s available at §n https://www.spigotmc.org/resources/74747/"));
        Message.InvalidSpigotResourceID = Message.replace(configGetString("Message.InvalidSpigotResourceID", "It looks like you are using an unsupported version of UniBan. Please manually look for update."));
        Message.FailedCheckingUpdate = Message.replace(configGetString("Message.FailedCheckingUpdate", "Error occurred when checking update"));
        Message.LoadedFromLocalCache = Message.replace(configGetString("Message.LoadedFromLocalCache", "Loaded %s banned players from ban-list cache."));
        Message.HelpMessageHeader = Message.replace(configGetString("Message.HelpMessageHeader", "Usage:"));
        Message.Processing = Message.replace(configGetString("Message.Processing", "Just a sec..."));
        Message.HelpMessageList = Message.replace(configGetStringList("Message.HelpMessageList"));
        if (Message.HelpMessageList.size() == 0) {
            Message.HelpMessageList = Arrays.asList("/uniban check <§lPlayer/UUID§r>",
                    "/uniban whitelist <“§ladd§r”/“§lremove§r”> <§lPlayer/UUID>",
                    "/uniban share <§lYour Server Hostname§r, eg. §nexample.com§r>",
                    "/uniban subscribe <§lSubscription Key§r>",
                    "/uniban exempt <§lServer Address§r>",
                    "/uniban reload");
        }
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
                if (configGetString("Subscription." + key + ".Host", "").equals(serverEntry.host)
                        && configGetInt("Subscription." + key + ".Port", 0) == (serverEntry.port)) {
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
