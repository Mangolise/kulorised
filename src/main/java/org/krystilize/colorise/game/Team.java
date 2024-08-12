package org.krystilize.colorise.game;

import net.minestom.server.tag.Tag;
import org.krystilize.colorise.Color;

import java.util.List;

public enum Team {
    BLUE(Color.RED, Color.BLUE),
    GREEN(Color.GREEN, Color.YELLOW);

    public static final Tag<Team> TAG = Tag.String("team").map(Team::valueOf, Team::name);

    private final List<Color> colors;
    Team(Color... colors) {
        this.colors = List.of(colors);
    }

    public List<Color> colors() {
        return colors;
    }

    public static Team getTeamFromColor(Color color) {
        for (Team team : Team.values()) {
            for (Color teamColor : team.colors) {
                if (teamColor == color) {
                    return team;
                }
            }
        }

        return null;
    }
}
