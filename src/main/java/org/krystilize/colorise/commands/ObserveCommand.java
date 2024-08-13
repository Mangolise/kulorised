package org.krystilize.colorise.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.krystilize.colorise.game.GameInstance;
import org.krystilize.colorise.queue.QueueSystem;

public class ObserveCommand extends Command {
    private final QueueSystem queue;
    private final Instance lobby;

    public ObserveCommand(QueueSystem queue, Instance lobby) {
        super("observe");
        this.queue = queue;
        this.lobby = lobby;

        setCondition((sender, s) -> sender instanceof Player);
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

        if (player.getInstance() != lobby) player.setInstance(lobby);
        player.sendMessage("You are exempt from queuing");
    }
}
