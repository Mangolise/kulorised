package net.mangolise.kulorised.commands;

import net.mangolise.kulorised.game.GameInstance;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;

public class LeaveCommand extends Command {

    public LeaveCommand() {
        super("leave");

        addSyntax(this::execute);
    }

    private void execute(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) {
            return;
        }

        GameInstance game = (GameInstance) player.getInstance();
        game.stop();
    }
}
