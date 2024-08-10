package org.krystilize.colorise.game;

import net.minestom.server.instance.Instance;

public record ColoriseGame(Instance instance, ColoredBlocks blocks) {
    public void start() {
        new HotbarColorController(this);
    }
}
