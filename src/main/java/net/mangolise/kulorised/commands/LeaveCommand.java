package net.mangolise.kulorised.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import net.mangolise.kulorised.game.GameInstance;

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
