package cc.eumc.uniban.serverinterface;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BukkitPlayerInfo<T> implements PlayerInfo {
    T p;
    public BukkitPlayerInfo(T p) {
        this.p = p;
    }

    @Override
    public @Nullable UUID getUUID() {
        if (!(p instanceof Player)) {
            return null;
        }
        return ((Player)p).getUniqueId();
    }

    @Override
    public @Nullable String getName() {
        if (!(p instanceof Player)) {
            return null;
        }
        return ((Player)p).getName();
    }

    @Override
    public void sendMessage(String msg) {
        if (p instanceof Player) {
            ((Player)p).sendMessage(msg);
        } else if (p instanceof CommandSender) {
            ((CommandSender)p).sendMessage(msg);
        }
    }
}
