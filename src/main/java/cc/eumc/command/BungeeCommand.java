package cc.eumc.command;

import cc.eumc.UniBanBungeePlugin;
import cc.eumc.controller.BungeeCommandController;
import cc.eumc.exception.CommandBreakException;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class BungeeCommand extends Command {
    UniBanBungeePlugin plugin;
    BungeeCommandController commandController;
    public BungeeCommand(UniBanBungeePlugin instance) {
        super("uniban", "uniban.admin");
        this.plugin = instance;
        commandController = new BungeeCommandController(instance);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            for (String line : commandController.executeCommand(args)) {
                sender.sendMessage(new TextComponent(line));
            }
        } catch (CommandBreakException e) {
            return;
        }
    }
}
