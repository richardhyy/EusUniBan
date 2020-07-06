package cc.eumc.uniban.serverinterface;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BungeePlayerInfo<T> implements PlayerInfo {
    T p;
    public BungeePlayerInfo(T p) {
        this.p = p;
    }

    @Override
    public @Nullable UUID getUUID() {
        if (!(p instanceof ProxiedPlayer)) {
            return null;
        }
        return ((ProxiedPlayer)p).getUniqueId();

    }

    @Override
    public String getName() {
        if (!(p instanceof ProxiedPlayer)) {
            return null;
        }
        return ((ProxiedPlayer)p).getName();
    }

    @Override
    public void sendMessage(String msg) {
        if (p instanceof ProxiedPlayer) {
            ((ProxiedPlayer)p).sendMessage(new TextComponent(msg));
        } else if (p instanceof CommandSender) {
            ((CommandSender)p).sendMessage(new TextComponent(msg));
        }
    }
}
