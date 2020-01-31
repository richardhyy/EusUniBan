package cc.eumc.controller;

import cc.eumc.exception.CommandBreakException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class CommandController {
    public List<String> executeCommand(String[] args) throws CommandBreakException {
        List<String> result = new ArrayList<>();
        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            sendHelp(result);
        }
        else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            doReload();
            result.add("[UniBan] Reloaded.");
        }
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("check")) {
                Boolean banned;
                try {
                    banned = isBannedOnline(UUID.fromString(args[1]));
                } catch (IllegalArgumentException e) {
                    Player player = Bukkit.getPlayer(args[1]);
                    if (player == null) {
                        result.add("[UniBan] Player " + args[1] + " does not exist.");
                        throw new CommandBreakException();
                    }
                    else
                        banned = isBannedOnline(player);
                }
                result.add("[UniBan] Player " + args[0] + " state: " + (banned?"§cbanned by at least 1 server":"§anormal"));
            }
            else {
                sendHelp(result);
            }
        }
        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("whitelist")) {
                if (args[1].equalsIgnoreCase("add")) {
                    try {
                        addWhitelist(UUID.fromString(args[2]));
                    } catch (IllegalArgumentException e) {
                        Player player = Bukkit.getPlayer(args[2]);
                        if (player == null) {
                            result.add("[UniBan] Player " + args[2] + " does not exist.");
                            throw new CommandBreakException();
                        }
                        else
                            addWhitelist(player.getUniqueId());
                    }
                    result.add("Player " + args[2] + " has been added to whitelist");
                }
                else if (args[1].equalsIgnoreCase("remove")) {
                    try {
                        removeWhitelist(UUID.fromString(args[2]));
                    } catch (IllegalArgumentException e) {
                        Player player = Bukkit.getPlayer(args[2]);
                        if (player == null) {
                            result.add("[UniBan] Player " + args[2] + " does not exist.");
                            throw new CommandBreakException();
                        }
                        else
                            removeWhitelist(player.getUniqueId());
                    }
                    result.add("Player " + args[2] + " has been removed from whitelist");
                }
            }
            else {
                sendHelp(result);
            }
        }
        return result;
    }

    void sendHelp(List<String> result) {
        result.add("[UniBan] /uniban check <Player/UUID>");
        result.add("[UniBan] /uniban whitelist <add/remove> <Player/UUID>");
        result.add("[UniBan] /uniban reload");
    }

    abstract void doReload();
    abstract boolean isBannedOnline(Player player);
    abstract boolean isBannedOnline(UUID uuid);
    abstract void addWhitelist(UUID uuid);
    abstract void removeWhitelist(UUID uuid);
}
