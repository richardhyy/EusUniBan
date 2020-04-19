package cc.eumc.uniban.util;

import cc.eumc.uniban.controller.UniBanController;
import net.craftminecraft.bungee.bungeeban.BanManager;
import net.craftminecraft.bungee.bungeeban.banstore.BanEntry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BungeeBanSupport {
    public static Set<UUID> fetchAllBanned(UniBanController controller) {
        List<BanEntry> banEntryList = BanManager.getBanList();
        if (banEntryList == null) return new HashSet<>();

        Set<UUID> bannedUUID = new HashSet<>();

        for (BanEntry banEntry : banEntryList) {
            if (controller.shouldIgnoreReason(banEntry.getReason())) {
                continue;
            }

            String uuidStr = banEntry.getBanned();

            // Check if it returns with UUID without dashes
            if (!uuidStr.contains("-")) {
                uuidStr = uuidStr.replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
                );
            }

            bannedUUID.add(UUID.fromString(uuidStr));

        }

        return bannedUUID;
    }
}
