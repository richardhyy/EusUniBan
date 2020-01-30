package cc.eumc.config;

import java.security.Key;

public class SubscriptionServerEntry {
    public final Key key;
    public final SubscriptionGroupEntry group;

    public SubscriptionServerEntry (Key key, SubscriptionGroupEntry groupEntry) {
        this.key = key;
        this.group = groupEntry;
    }
}
