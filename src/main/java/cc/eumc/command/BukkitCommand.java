package cc.eumc.command;

import cc.eumc.UniBanBukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BukkitCommand implements CommandExecutor, TabExecutor {
    UniBanBukkitPlugin plugin;
    private String[] commands = {"help", "check", "whitelist", "reload"};
    private String[] whitelistSubcommands = {"add", "remove"};

    public BukkitCommand(UniBanBukkitPlugin instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("uniban.admin")) {
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
        else {
            sender.sendMessage("[UniBan] §eSorry.");
        }
        return true;
    }

    void sendHelp(CommandSender sender) {
        sender.sendMessage("[UniBan] /uniban check <Player/UUID>");
        sender.sendMessage("[UniBan] /uniban whitelist <add/remove> <Player/UUID>");
        sender.sendMessage("[UniBan] /uniban reload");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length > 2)
            return new ArrayList<>();
        else if (args.length == 2)
            if (args[0].equalsIgnoreCase("whitelist"))
                return Arrays.stream(whitelistSubcommands).filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
            else
                return new ArrayList<>();
        else if (args.length == 1)
            return Arrays.stream(commands).filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        else
            return Arrays.asList(commands);
    }
}
