package org.krystilize.colorise.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class ShoutCommand extends Command {

    public ShoutCommand() {
        super("shout");

        setDefaultExecutor((sender, context) -> sender.sendMessage("Usage: /shout <message>"));

        addSyntax(this::shoutMsg, ArgumentType.String("msg"));
    }

    private void shoutMsg(CommandSender sender, CommandContext context) {
        String msgText = context.get("msg");

        String name = sender instanceof Player ? ((Player) sender).getUsername() : "Console";
        String msg = "[SHOUT] " + name + ": " + msgText;

        for (Player p : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            p.sendMessage(msg);
        }
    }
}
