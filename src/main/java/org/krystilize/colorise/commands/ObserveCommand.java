package org.krystilize.colorise.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import org.krystilize.colorise.Util;
import org.krystilize.colorise.game.GameInstance;
import org.krystilize.colorise.queue.QueueSystem;

public class ObserveCommand extends Command {
    private final QueueSystem queue;

    public ObserveCommand(QueueSystem queue) {
        super("observe");
        this.queue = queue;

        setCondition((sender, s) -> sender instanceof Player player && Util.ADMINS.contains(player.getUsername()));

        addSyntax(this::executeNoArgs);
    }

    private void executeNoArgs(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You must be a player to execute this command.");
            return;
        }

        // Make them not exempt
        if (queue.isExempt(player)) {
            queue.setPlayerExempt(player, false);
            queue.addPlayer(player);
            player.sendMessage("You have been made not exempt");
            return;
        }

        // Make them exempt
        queue.setPlayerExempt(player, true);
        if (queue.isQueued(player)) queue.removePlayer(player);
        if (player.getInstance() instanceof GameInstance game) game.stop();

        player.sendMessage("You are exempt from queuing");
    }
}
