package org.krystilize.colorise.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.utils.entity.EntityFinder;
import org.krystilize.colorise.Util;
import org.krystilize.colorise.event.PlayerJoinAcceptEvent;
import org.krystilize.colorise.game.GameInstance;
import org.krystilize.colorise.queue.JoinInviteSystem;

import static net.minestom.server.command.builder.arguments.ArgumentType.Entity;
import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

public class LeaveCommand extends Command {

    public LeaveCommand() {
        super("leave");

        setDefaultExecutor(this::usage);
    }

    private void usage(CommandSender sender, CommandContext context) {
        Player player = (Player) sender;

        if (!(player.getInstance() instanceof GameInstance game)) {
            player.sendMessage(Component.text("You are not in a game!", NamedTextColor.RED));
            return;
        }

        game.stop();
    }
}
