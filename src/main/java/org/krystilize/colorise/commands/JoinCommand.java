package org.krystilize.colorise.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import static net.minestom.server.command.builder.arguments.ArgumentType.*;

import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.utils.entity.EntityFinder;
import org.krystilize.colorise.Util;
import org.krystilize.colorise.event.PlayerJoinAcceptEvent;
import org.krystilize.colorise.queue.JoinInviteSystem;

public class JoinCommand extends Command {

    public JoinCommand() {
        super("join");

        setDefaultExecutor((sender, context) -> sender.sendMessage("Usage: /join <username>"));

        addConditionalSyntax(Util::isPlayer, this::usagePlayer, Entity("player").onlyPlayers(true).singleEntity(true));
        addConditionalSyntax(Util::isPlayer, this::usageAcceptPlayer, Literal("accept"), Entity("player").onlyPlayers(true).singleEntity(true));
    }

    private void usagePlayer(CommandSender sender, CommandContext context) {
        Player player = (Player) sender;
        Player other = context.<EntityFinder>get("player").findFirstPlayer(sender);

        if (other == null) {
            player.sendMessage("Usage: /join <player>");
            return;
        }

        if (player.getUuid().equals(other.getUuid())) {
            player.sendMessage(Component.text("You can't invite yourself. Sorry D:"));
            return;
        }

        JoinInviteSystem.invite(player.getUuid(), other.getUuid());
        player.sendMessage(Component.text("Invited " + other.getUsername() + " to play together. They have " + JoinInviteSystem.INVITE_TIMEOUT_SECONDS + " seconds to accept."));

        other.sendMessage(Component.text()
                .append(Component.text(player.getUsername() + " has invited you to play together. You have 60 seconds to accept. "))
                .append(Component.text("Click here to accept.")
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/join accept " + player.getUsername()))
                        .hoverEvent(HoverEvent.showText(Component.text("Run /join accept " + player.getUsername())))
                        .color(TextColor.fromHexString("#d69f09"))));
    }

    private void usageAcceptPlayer(CommandSender sender, CommandContext context) {
        Player player = (Player) sender;
        Player other = context.<EntityFinder>get("player").findFirstPlayer(sender);

        if (other == null) {
            player.sendMessage("Usage: /join accept <player>");
            return;
        }

        if (player.getUuid().equals(other.getUuid())) {
            player.sendMessage(Component.text("You can't accept your own invite."));
            return;
        }

        if (!JoinInviteSystem.doesInviteExist(other.getUuid(), player.getUuid())) {
            player.sendMessage(Component.text("You do not have an invite from this player!", TextColor.color(255, 0, 0)));
            return;
        }

        player.sendMessage(Component.text("Accepted " + other.getUsername() + "'s invite. Creating a game for you both..."));
        other.sendMessage(Component.text(player.getUsername() + " has accepted your invite. Creating a game for you both..."));

        PlayerJoinAcceptEvent event = new PlayerJoinAcceptEvent(player, other);
        EventDispatcher.call(event);
    }
}
