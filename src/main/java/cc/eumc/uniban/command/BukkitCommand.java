package cc.eumc.uniban.command;

import cc.eumc.uniban.UniBanBukkitPlugin;
import cc.eumc.uniban.controller.BukkitCommandController;
import cc.eumc.uniban.exception.CommandBreakException;
import cc.eumc.uniban.serverinterface.BukkitPlayerInfo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BukkitCommand implements CommandExecutor, TabExecutor {
    UniBanBukkitPlugin plugin;
    BukkitCommandController commandController;
    private String[] commands = {"help", "check", "lookup", "subscribe", "share", "whitelist", "exempt", "reload"};
    private String[] whitelistSubcommands = {"add", "remove"};

    public BukkitCommand(UniBanBukkitPlugin instance) {
        this.plugin = instance;
        commandController = new BukkitCommandController(instance, plugin.getBukkitConfig());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("uniban.admin")) {
            /*
            if (args.length == 2 && args[0].equalsIgnoreCase("lookupuuid")) {
                sender.sendMessage("");
                UniBanController.nameToUUID(args[1]);
                return true;
            }
             */

            List<String> msgLines = null;
            try {
                msgLines = commandController.executeCommand(args, plugin.getController(), new BukkitPlayerInfo<>(sender));
            } catch (CommandBreakException ignored) {
                if (msgLines != null) {
                    for (String line : msgLines) {
                        sender.sendMessage(line);
                    }
                }
            }
        }
        else {
            sender.sendMessage("[UniBan] §eSorry.");
        }
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Fix: tab complete still work even if a player does not have permission "uniban.admin"
        if (!sender.hasPermission("uniban.admin")) return new ArrayList<>();

        if (args.length > 2)
            return new ArrayList<>();
        else if (args.length == 2)
            if (args[0].equalsIgnoreCase("whitelist"))
                // Fix: Tab complete won't work for sub-commands
                return Arrays.stream(whitelistSubcommands).filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
            else if (args[0].equalsIgnoreCase("exempt"))
                return Arrays.stream(plugin.getBukkitConfig().getSubscriptions()).filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
            else
                return new ArrayList<>();
        else if (args.length == 1)
            return Arrays.stream(commands).filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        else
            return Arrays.asList(commands);
    }
}
