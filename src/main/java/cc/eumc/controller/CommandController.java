package cc.eumc.controller;

import cc.eumc.config.PluginConfig;
import cc.eumc.config.ServerEntry;
import cc.eumc.exception.CommandBreakException;
import cc.eumc.util.Encryption;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class CommandController {
    final static String MSGPREFIX = "UniBan §3> §r";
    final static Key SHARING_KEY = Encryption.getKeyFromString("UniBanSubscription");
    PluginConfig pluginConfig;

    public CommandController(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    public List<String> executeCommand(String[] args) throws CommandBreakException {
        List<String> result = new ArrayList<>();
        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            sendHelp(result);
        }
        else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                doReload();
                result.add(MSGPREFIX + " Reloaded.");
            }
            else {
                sendHelp(result);
            }
        }
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("check")) {
                Boolean banned;
                try {
                    banned = isBannedOnline(UUID.fromString(args[1]));
                } catch (IllegalArgumentException e) {
                    Player player = Bukkit.getPlayer(args[1]);
                    if (player == null) {
                        result.add(MSGPREFIX + " Player " + args[1] + " does not exist.");
                        throw new CommandBreakException();
                    }
                    else
                        banned = isBannedOnline(player);
                }
                result.add(MSGPREFIX + " Player " + args[0] + " state: " + (banned?"§cbanned by at least 1 server":"§anormal"));
            }
            else if (args[0].equalsIgnoreCase("subscribe")) {
                // T√ODO A quick way to add subscription
                String subscriptionKey;
                // subscriptionKey: <host>:<port>@<password>
                if ((subscriptionKey = Encryption.decrypt(args[1], SHARING_KEY)) != null && (subscriptionKey.contains(":")&&subscriptionKey.contains("@"))) {
                    String[] splited = subscriptionKey.split("@", 2);
                    if (splited.length != 2) {
                        result.add(MSGPREFIX + "§eInvalid subscription key: ");
                        result.add(MSGPREFIX + args[1] + "->" + subscriptionKey);
                    }
                    else {
                        String address = splited[0];
                        pluginConfig.addSubscription(new ServerEntry(address), splited[1], true);
                        result.add(MSGPREFIX + "Successfully added " + address + " to your subscription list.");
                    }
                }
                else {
                    result.add(MSGPREFIX + " Invalid subscription key.");
                }
            }
            else if (args[0].equalsIgnoreCase("share")) {
                result.add("Here's the subscription key of your server which contains your address and connection password:");
                result.add(MSGPREFIX + Encryption.encrypt(args[1] + ":" + PluginConfig.Port + "@" + PluginConfig.Password, SHARING_KEY));
            }
            else if (args[0].equalsIgnoreCase("exempt")) {
                result.add(MSGPREFIX + (pluginConfig.removeSubscription(args[1], false)?("Successfully exempted server " + args[1] + " from subscription list temporarily."):"Failed exempting. Does that subscription exist?"));
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
                            result.add(MSGPREFIX + " Player " + args[2] + " does not exist.");
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
                            result.add(MSGPREFIX + " Player " + args[2] + " does not exist.");
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
        result.add(MSGPREFIX + "/uniban check <§lPlayer/UUID§r>");
        result.add(MSGPREFIX + "/uniban whitelist <“§ladd§r”/“§lremove§r”> <§lPlayer/UUID>");
        result.add(MSGPREFIX + "/uniban share <§lYour Server Hostname§r, eg. §nexample.com§r>");
        result.add(MSGPREFIX + "/uniban subscribe <§lSubscription Key§r>");
        result.add(MSGPREFIX + "/uniban exempt <§lServer Address§r>");
        result.add(MSGPREFIX + "/uniban reload");
    }

    abstract void doReload();
    abstract boolean isBannedOnline(Player player);
    abstract boolean isBannedOnline(UUID uuid);
    abstract void addWhitelist(UUID uuid);
    abstract void removeWhitelist(UUID uuid);
}
