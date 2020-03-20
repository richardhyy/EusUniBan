package cc.eumc.uniban.util;

import cc.eumc.uniban.config.ThirdPartySupportConfig;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.util.*;

public class VanillaListSupport {
    static class VanillaBan {
        public String uuid;
        public String name;
        public String created;
        public String source;
        public String expires;
        public String reason;
    }

    public static Set<UUID> fetchAllBanned() {
        Set<UUID> banned = new HashSet<>();
        File file = new File(ThirdPartySupportConfig.DataFolder, "banned-players.json");
        if (!file.exists()) {
            return banned;
        }

        try {
            FileReader reader = new FileReader(file);
            Gson g = new Gson();
            List<VanillaBan> vanillaBanList;
            vanillaBanList = Arrays.asList(g.fromJson(reader, VanillaBan[].class));

            try {
                vanillaBanList.forEach(vanillaBan -> banned.add(UUID.fromString(vanillaBan.uuid)));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return banned;
    }
}
