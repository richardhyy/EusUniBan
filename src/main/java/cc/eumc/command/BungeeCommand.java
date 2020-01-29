package cc.eumc.command;

import cc.eumc.UniBanBungeePlugin;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BungeeCommand extends Command {
    UniBanBungeePlugin plugin;
    public BungeeCommand(UniBanBungeePlugin instance) {
        super("uniban", "uniban.admin");
        this.plugin = instance;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            sendHelp(sender);
        }
        else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.reloadController();
            plugin.registerTask();
            sender.sendMessage("[UniBan] Reloaded.");
        }
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("check")) {
                Boolean banned;
                try {
                    banned = plugin.getController().isBannedOnline(UUID.fromString(args[1]));
                } catch (IllegalArgumentException e) {
                    Player player = Bukkit.getPlayer(args[1]);
                    if (player == null) {
                        sender.sendMessage("[UniBan] Player " + args[1] + " does not exist.");
                        return true;
                    }
                    else
                        banned = plugin.getController().isBannedOnline(player);
                }
                sender.sendMessage("[UniBan] Player " + args[0] + " state: " + (banned?"§cbanned":"§anormal"));
            }
            else {
                sendHelp(sender);
            }
        }
        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("whitelist")) {
                if (args[1].equalsIgnoreCase("add")) {
                    try {
                        plugin.getController().addWhitelist(UUID.fromString(args[2]));
                    } catch (IllegalArgumentException e) {
                        Player player = Bukkit.getPlayer(args[2]);
                        if (player == null) {
                            sender.sendMessage("[UniBan] Player " + args[2] + " does not exist.");
                            return true;
                        }
                        else
                            plugin.getController().addWhitelist(player.getUniqueId());
                    }
                    sender.sendMessage("Player " + args[2] + " has been added to whitelist");
                }
                else if (args[1].equalsIgnoreCase("remove")) {
                    try {
                        plugin.getController().removeWhitelist(UUID.fromString(args[2]));
                    } catch (IllegalArgumentException e) {
                        Player player = Bukkit.getPlayer(args[2]);
                        if (player == null) {
                            sender.sendMessage("[UniBan] Player " + args[2] + " does not exist.");
                            return true;
                        }
                        else
                            plugin.getController().removeWhitelist(player.getUniqueId());
                    }
                    sender.sendMessage("Player " + args[2] + " has been removed from whitelist");
                }
            }
            else {
                sendHelp(sender);
            }
        }
    }
}
