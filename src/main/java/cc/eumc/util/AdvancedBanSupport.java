package cc.eumc.util;

import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.utils.SQLQuery;

import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AdvancedBanSupport {
    public static Set<UUID> fetchAllBanned() {
        if (DatabaseManager.get() == null) return new HashSet<>();

        Set<UUID> bannedUUID = new HashSet<>();

        try {
            ResultSet result = DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_ALL_PUNISHMENTS);
            while (result.next()) {
                try {
                    String uuidStr = result.getString("uuid");

                    // AdvancedBan returns with UUID without dashes
                    if (!uuidStr.contains("-")) {
                        uuidStr = uuidStr.replaceFirst(
                                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
                        );
                    }

                    bannedUUID.add(UUID.fromString(uuidStr));
                }
                catch (IllegalArgumentException ignore) {
                    System.out.println("Illegal UUID returned from AdvancedBan: " + result.getString("uuid"));
                }
                //System.out.println(result.getString("uuid"));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return bannedUUID;
    }
}
