package cc.eumc.uniban.controller;

import cc.eumc.uniban.config.Message;
import cc.eumc.uniban.config.PluginConfig;
import cc.eumc.uniban.config.ServerEntry;
import cc.eumc.uniban.exception.CommandBreakException;
import cc.eumc.uniban.serverinterface.PlayerInfo;
import cc.eumc.uniban.util.Encryption;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class CommandController {
    final String MSGPREFIX;
    public final static Key SHARING_KEY = Encryption.getKeyFromString("UniBanSubscription");
    PluginConfig pluginConfig;

    public CommandController(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
        MSGPREFIX = Message.MessagePrefix;
    }

    public List<String> executeCommand(String[] args, UniBanController controller, PlayerInfo sender) throws CommandBreakException {
        List<String> result = new ArrayList<>();
        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            sendHelp(result);
        }
        else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                doReload();
                result.add(MSGPREFIX + Message.Reloaded);
            }
            else {
                sendHelp(result);
            }
        }
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("lookup")) {
                sender.sendMessage(MSGPREFIX + Message.Processing);

                controller.runTaskLater(() -> {
                    UUID uuid = UniBanController.nameToUUID(args[1]);
                    if (uuid == null) {
                        sender.sendMessage(MSGPREFIX + String.format(Message.PlayerNotExist, args[1]));
                    }
                    else {
                        sender.sendMessage(MSGPREFIX + args[1] + ": " + uuid.toString());
                    }
                }, 1);

                throw new CommandBreakException();
            }
            else if (args[0].equalsIgnoreCase("check")) {

                controller.runTaskLater(() -> {
                    UUID uuid = null;
                    try {
                        uuid = UUID.fromString(args[1]);
                    } catch (Exception e) {
                        sender.sendMessage(MSGPREFIX + Message.Processing);
                        uuid = UniBanController.nameToUUID(args[1]);
                    }
                    if (uuid == null) {
                        sender.sendMessage(MSGPREFIX + String.format(Message.PlayerNotExist, args[1]));
                        return;
                    }
                    boolean banned = isBannedOnline(uuid);
                    sender.sendMessage(MSGPREFIX + String.format(Message.PlayerState, args[1], banned?Message.PlayerBanned:Message.PlayerNormal));
                    if (banned) {
                        getBannedServerList(uuid).forEach(sender::sendMessage);
                    }
                }, 1);
                throw new CommandBreakException();
            }
            else if (args[0].equalsIgnoreCase("subscribe")) {
                // T√ODO A quick way to add subscription
                String subscriptionKey;
                // subscriptionKey: <host>:<port>@<password>
                if ((subscriptionKey = Encryption.decrypt(args[1], SHARING_KEY)) != null && (subscriptionKey.contains(":")&&subscriptionKey.contains("@"))) {
                    String[] split = subscriptionKey.split("@", 2);
                    if (split.length != 2) {
                        result.add(MSGPREFIX + Message.InvalidSubscriptionKey);
                        result.add(MSGPREFIX + args[1] + "->" + subscriptionKey);
                    }
                    else {
                        String address = split[0];
                        pluginConfig.addSubscription(new ServerEntry(address), split[1], true);
                        result.add(MSGPREFIX + String.format(Message.SubscriptionKeyAdded, address));
                    }
                }
                else {
                    result.add(MSGPREFIX + Message.InvalidSubscriptionKey);
                }
            }
            else if (args[0].equalsIgnoreCase("share")) {
                String key = Encryption.encrypt(args[1] + ":" + PluginConfig.Port + "@" + PluginConfig.Password, SHARING_KEY);
                if (key == null) {
                    result.add(MSGPREFIX + String.format(Message.Error, "Failed generating Subscription Key."));
                }
                else {
                    result.add(MSGPREFIX + Message.YourSubscriptionKey);
                    try {
                        result.add(MSGPREFIX + String.format(Message.SubscriptionKeyLink, URLEncoder.encode(key, "UTF-8")));
                    } catch (UnsupportedEncodingException e) {
                        result.add(MSGPREFIX + key);
                    }
                }
            }
            else if (args[0].equalsIgnoreCase("exempt")) {
                result.add(MSGPREFIX + (pluginConfig.removeSubscription(args[1], false)?(String.format(Message.SubscriptionExempted, args[1])): String.format(Message.FailedExempting, args[1])));
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
                        PlayerInfo player = controller.getPlayerInfoFromUUID(UniBanController.nameToUUID(args[2]));
                        if (player == null) {
                            result.add(MSGPREFIX + String.format(Message.PlayerNotExist, args[2]));
                            throw new CommandBreakException();
                        }
                        else
                            addWhitelist(player.getUUID());
                    }
                    result.add(MSGPREFIX + String.format(Message.WhitelistAdded, args[2]));
                }
                else if (args[1].equalsIgnoreCase("remove")) {
                    try {
                        removeWhitelist(UUID.fromString(args[2]));
                    } catch (IllegalArgumentException e) {
                        PlayerInfo player = controller.getPlayerInfoFromUUID(UniBanController.nameToUUID(args[2]));
                        if (player == null) {
                            result.add(MSGPREFIX + String.format(Message.PlayerNotExist, args[2]));
                            throw new CommandBreakException();
                        }
                        else
                            removeWhitelist(player.getUUID());
                    }
                    result.add(MSGPREFIX + String.format(Message.WhitelistRemoved, args[2]));
                }
            }
            else {
                sendHelp(result);
            }
        }
        return result;
    }

    void sendHelp(List<String> result) {
        result.add(MSGPREFIX + Message.HelpMessageHeader);
        result.addAll(Message.HelpMessageList);
    }

    abstract void doReload();
    abstract boolean isBannedOnline(UUID uuid);
    abstract List<String> getBannedServerList(UUID uuid);
    abstract void addWhitelist(UUID uuid);
    abstract void removeWhitelist(UUID uuid);
}
