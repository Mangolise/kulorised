package org.krystilize.colorise.game;

import net.minestom.server.entity.Player;
import org.krystilize.colorise.queue.QueueSystem;

import java.util.List;

public record ColoriseGame(List<Player> players, QueueSystem queue) {
}
