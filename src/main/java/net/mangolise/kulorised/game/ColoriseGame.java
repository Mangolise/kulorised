package net.mangolise.kulorised.game;

import net.minestom.server.entity.Player;
import net.mangolise.kulorised.queue.QueueSystem;

import java.util.List;

public record ColoriseGame(List<Player> players, QueueSystem queue) {
}
