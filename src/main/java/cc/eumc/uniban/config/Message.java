package cc.eumc.uniban.config;

import java.util.ArrayList;
import java.util.List;

public class Message {
    public static String WarningMessage;
    public static String BannedOnlineKickMessage;
    public static String IgnoredOP;
    public static String MessagePrefix;
    public static String Error;
    public static String Reloaded;
    public static String PlayerNotExist;
    public static String PlayerState;
    public static String PlayerBanned;
    public static String PlayerNormal;
    public static String InvalidSubscriptionKey;
    public static String SubscriptionKeyAdded;
    public static String YourSubscriptionKey;
    public static String SubscriptionKeyLink;
    public static String SubscriptionExempted;
    public static String FailedExempting;
    public static String WhitelistAdded;
    public static String WhitelistRemoved;
    public static String SubscriptionsHeader;
    public static String ThirdPartyPluginSupportHeader;
    public static String Encrypted;
    public static String BroadcastStarted;
    public static String BroadcastActiveModeEnabled;
    public static String BroadcastViaServerListPing;
    public static String BroadcastFailed;
    public static String PluginEnabled;
    public static String PluginNotFound;
    public static String UpToDate;
    public static String NewVersionAvailable;
    public static String InvalidSpigotResourceID;
    public static String FailedCheckingUpdate;
    public static String LoadedFromLocalCache;
    public static String Processing;
    public static String HelpMessageHeader;
    public static List<String> HelpMessageList;

    public static String replace(String str) {
        return str.replace("&", "§");
    }

    public static List<String> replace(List<String> strList) {
        List<String> resultList = new ArrayList<>();
        strList.forEach(str -> {
            resultList.add(replace(str));
        });
        return resultList;
    }
}
