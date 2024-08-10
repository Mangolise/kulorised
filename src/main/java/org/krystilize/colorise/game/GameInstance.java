package org.krystilize.colorise.game;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.SharedInstance;

import java.util.Set;
import java.util.UUID;

public abstract class GameInstance extends SharedInstance {

    protected final ColoriseGame game;
    protected final Player player1;
    protected final Player player2;

    public GameInstance(GameInfo info) {
        super(UUID.randomUUID(), info.parent());
        this.game = new ColoriseGame(info.players(), new ColoredBlocks(this, info.blocks()));
        this.player1 = info.players().get(0);
        this.player2 = info.players().get(1);
    }

    protected abstract Set<Mechanic> mechanics();
    private boolean started = false;
    public synchronized void start() {
        if (started) {
            return;
        }
        started = true;
        for (Mechanic mechanic : this.mechanics()) {
            mechanic.setup(game, this.eventNode(), this);
        }
    }
}
