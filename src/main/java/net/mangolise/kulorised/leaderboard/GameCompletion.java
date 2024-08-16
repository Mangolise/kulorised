package net.mangolise.kulorised.leaderboard;

import net.mangolise.kulorised.Util;

import java.util.Set;

public record GameCompletion(Set<String> players, long time) {

    public String playersString() {
        return String.join(", ", players);
    }

    public String timeString() {
        return Util.getFormattedTime(time);
    }
}
