package net.mangolise.kulorised.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import net.mangolise.kulorised.leaderboard.LeaderboardManager;

public class ToggleScoreboardCommand extends Command {

    public ToggleScoreboardCommand() {
        super("togglescoreboard");

        setCondition((sender, s) -> sender instanceof Player);
        addSyntax(this::execute);
    }

    private void execute(CommandSender sender, CommandContext context) {
        boolean isEnabled = LeaderboardManager.toggleScoreboard((Player) sender);

        if (isEnabled) {
            sender.sendMessage("Scoreboard enabled.");
        } else {
            sender.sendMessage("Scoreboard disabled.");
        }
    }
}
