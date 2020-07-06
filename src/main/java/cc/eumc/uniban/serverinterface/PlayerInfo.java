package cc.eumc.uniban.serverinterface;

import java.util.UUID;

public interface PlayerInfo {
    UUID getUUID();
    String getName();
    void sendMessage(String msg);
}
