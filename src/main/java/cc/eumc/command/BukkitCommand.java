package cc.eumc.command;

import cc.eumc.UniBanBukkitPlugin;
import cc.eumc.controller.BukkitCommandController;
import cc.eumc.exception.CommandBreakException;
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
    private String[] commands = {"help", "check", "subscribe", "share", "whitelist", "exempt", "reload"};
    private String[] whitelistSubcommands = {"add", "remove"};

    public BukkitCommand(UniBanBukkitPlugin instance) {
        this.plugin = instance;
        commandController = new BukkitCommandController(instance, plugin.getBukkitConfig());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("uniban.admin")) {
            try {
                for (String line : commandController.executeCommand(args)) {
                    sender.sendMessage(line);
                }
            } catch (CommandBreakException e) {
                return true;
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
