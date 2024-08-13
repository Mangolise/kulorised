package org.krystilize.colorise.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.krystilize.colorise.leaderboard.GameCompletion;
import org.krystilize.colorise.leaderboard.LeaderboardManager;

import java.util.List;

public class LeaderboardCommand extends Command {

    public LeaderboardCommand() {
        super("leaderboard");

        addSyntax(this::execute);
    }

    private void execute(CommandSender sender, CommandContext context) {
        List<GameCompletion> top10 = LeaderboardManager.getTopCompletions(10);

        sender.sendMessage(Component
                .text("Top 10 completions:")
                .color(TextColor.fromHexString("#70ff77"))
                .decoration(TextDecoration.BOLD, true));

        if (top10.isEmpty()) {
            sender.sendMessage(Component.text("No completions yet!").color(TextColor.fromHexString("#80ffee")));
            return;
        }

        for (int i = 0; i < top10.size(); i++) {
            GameCompletion completion = top10.get(i);

            sender.sendMessage(Component
                    .text((i + 1) + ". " + completion.timeString() + " - " + completion.playersString())
                    .color(TextColor.fromHexString(LeaderboardManager.getColourForPlace(i))));
        }
    }
}
