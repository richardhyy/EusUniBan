package cc.eumc.config;

public class SubscriptionGroupEntry {
    public final String groupName;
    public final int WarnThreshold;
    public final int BanThreshold;
    public final boolean IsDefault;

    public SubscriptionGroupEntry(String groupName, int warnThreshold, int banThreshold, boolean isDefault) {
        this.groupName = groupName;
        this.WarnThreshold = warnThreshold;
        this.BanThreshold = banThreshold;
        this.IsDefault = isDefault;
    }
}
