package org.krystilize.colorise.game;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.List;

public record ColoriseGame(List<Player> players, Instance instance, ColoredBlocks blocks) {
    public void start() {
        new HotbarColorController(this);
    }
}
