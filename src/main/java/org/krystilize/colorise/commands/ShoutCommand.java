package org.krystilize.colorise.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.krystilize.colorise.Util;

public class ShoutCommand extends Command {

    public ShoutCommand() {
        super("shout");

        setDefaultExecutor((sender, context) -> sender.sendMessage("Usage: /shout <message>"));

        addSyntax(this::shoutMsg, ArgumentType.String("msg"));
    }

    private void shoutMsg(CommandSender sender, CommandContext context) {
        String msgText = context.get("msg");

        Component message = Util.formatMessage("SHOUT", sender, msgText);

        for (Player p : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            p.sendMessage(message);
        }
    }
}
