package org.krystilize.colorise.leaderboard;

import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LeaderboardManager {
    private static final List<GameCompletion> completions = new ArrayList<>();

    public static List<GameCompletion> getTopCompletions(int amount) {
        return completions.subList(0, Math.min(amount, completions.size())).stream().sorted(Comparator.comparingLong(GameCompletion::time)).toList();
    }

    public static void addCompletion(Set<String> players, long time) {
        completions.add(new GameCompletion(players, time));
    }

    public static boolean addCompletionPlayers(Set<Player> players, long time) {
        GameCompletion best = getTopCompletions(1).stream().findFirst().orElse(null);

        Set<String> playerNames = players.stream().map(Player::getUsername).collect(Collectors.toSet());
        addCompletion(playerNames, time);

        return best == null || best.time() > time;
    }
}
