package org.krystilize.colorise.leaderboard;

import org.krystilize.colorise.Util;

import java.util.Set;

public record GameCompletion(Set<String> players, long time) {

    public String playersString() {
        return String.join(", ", players);
    }

    public String timeString() {
        return Util.getFormattedTime(time);
    }
}
