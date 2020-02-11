package cc.eumc.uniban.command;

import cc.eumc.uniban.UniBanBungeePlugin;
import cc.eumc.uniban.controller.BungeeCommandController;
import cc.eumc.uniban.exception.CommandBreakException;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

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
        try {
            for (String line : commandController.executeCommand(args)) {
                sender.sendMessage(new TextComponent(line));
            }
        } catch (CommandBreakException e) {
            return;
        }
    }
}
