package org.krystilize.colorise.queue;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import org.krystilize.colorise.Util;
import org.krystilize.colorise.game.ColoriseGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record QueueSystem(Instance lobby) {
    public QueueSystem {
    }

    private static Tag<List<Player>> QUEUED_PLAYERS = Tag.<List<Player>>Transient("queued_players").defaultValue(new ArrayList<>());

    public void addPlayer(Player player) {
        if (player.getInstance() == null || player.getInstance() != lobby) {
            player.setInstance(lobby);
        }
        Util.log(player.getUsername() + " joined the queue");

        // Add the player to the queue
        lobby.updateTag(QUEUED_PLAYERS, players -> {
            players.add(player);
            return players;
        });
    }

    public void updateQueue() {

    }

    public interface GameCreator {
        ColoriseGame createGame(Set<Player> players);
    }
}
