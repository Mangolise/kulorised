package org.krystilize.colorise.leaderboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.scoreboard.Sidebar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LeaderboardManager {
    private static final List<GameCompletion> completions = new ArrayList<>();
    private static final Sidebar leaderboard = new Sidebar(Component.text("Leaderboard").color(NamedTextColor.GOLD));

    public static List<GameCompletion> getTopCompletions(int amount) {
        return completions.subList(0, Math.min(amount, completions.size())).stream().sorted(Comparator.comparingLong(GameCompletion::time)).toList();
    }

    public static String getColourForPlace(int place) {
        return switch (place) {
            case 0 -> "#fff200";
            case 1 -> "#b0b0b0";
            case 2 -> "#5c3400";
            default -> "#80ffee";
        };
    }

    public static void addCompletion(Set<String> players, long time) {
        completions.add(new GameCompletion(players, time));
    }

    public static boolean addCompletionPlayers(Set<Player> players, long time) {
        GameCompletion best = getTopCompletions(1).stream().findFirst().orElse(null);

        Set<String> playerNames = players.stream().map(Player::getUsername).collect(Collectors.toSet());
        addCompletion(playerNames, time);

        updateScoreboard();
        return best == null || best.time() > time;
    }

    public static void setup() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, e ->
                leaderboard.addViewer(e.getPlayer()));
        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, e ->
                leaderboard.removeViewer(e.getPlayer()));

        updateScoreboard();
    }

    public static boolean toggleScoreboard(Player player) {
        if (leaderboard.getViewers().contains(player)) {
            leaderboard.removeViewer(player);
            return false;
        } else {
            leaderboard.addViewer(player);
            return true;
        }
    }

    private static void updateScoreboard() {
        // Add all the players scores
        for (Sidebar.ScoreboardLine line : leaderboard.getLines()) {
            leaderboard.removeLine(line.getId());
        }

        List<GameCompletion> top = getTopCompletions(5);

        Sidebar.ScoreboardLine disableInfo = new Sidebar.ScoreboardLine("disable", Component.text("Toggle with /togglescoreboard"), -1);
        if (top.isEmpty()) {
            leaderboard.createLine(new Sidebar.ScoreboardLine("0", Component.text("No completions").color(NamedTextColor.GRAY), 0));
            leaderboard.createLine(disableInfo);
            return;
        }

        for (int i = 0; i < top.size(); i++) {
            GameCompletion completion = top.get(i);

            Sidebar.ScoreboardLine line = new Sidebar.ScoreboardLine("" + i, Component
                    .text((i+1) + ". " + completion.timeString() + " - " + completion.playersString())
                    .color(TextColor.fromHexString(getColourForPlace(i))), top.size()-i);

            leaderboard.createLine(line);
        }

        leaderboard.createLine(disableInfo);
    }
}
