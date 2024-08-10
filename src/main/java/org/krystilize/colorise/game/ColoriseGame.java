package org.krystilize.colorise.game;

import net.minestom.server.entity.Player;

import java.util.List;

public record ColoriseGame(List<Player> players, ColoredBlocks blocks) {
}
