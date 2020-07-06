package cc.eumc.uniban.command;

import cc.eumc.uniban.UniBanBungeePlugin;
import cc.eumc.uniban.controller.BungeeCommandController;
import cc.eumc.uniban.exception.CommandBreakException;
import cc.eumc.uniban.serverinterface.BungeePlayerInfo;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;

public class BungeeCommand extends Command {
    UniBanBungeePlugin plugin;
    BungeeCommandController commandController;
    public BungeeCommand(UniBanBungeePlugin instance) {
        super("uniban", "uniban.admin");
        this.plugin = instance;
        commandController = new BungeeCommandController(instance, plugin.getBungeeConfig());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        /*
        if (args.length == 2 && args[0].equalsIgnoreCase("lookupuuid")) {
            sender.sendMessage(new TextComponent(UniBanController.nameToUUID(args[1]).toString()));
            return;
        }
         */

        List<String> msgLines = null;
        try {
            msgLines = commandController.executeCommand(args, plugin.getController(), new BungeePlayerInfo<>(sender));
            for (String line : msgLines) {
                sender.sendMessage(new TextComponent(line));
            }
        } catch (CommandBreakException ignored) {
            if (msgLines != null) {
                for (String line : msgLines) {
                    sender.sendMessage(new TextComponent(line));
                }
            }
        }
    }
}
