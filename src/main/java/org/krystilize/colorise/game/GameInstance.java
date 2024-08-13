package org.krystilize.colorise.game;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.tag.Tag;
import org.krystilize.colorise.Server;
import org.krystilize.colorise.Util;
import org.krystilize.colorise.game.mechanic.Mechanic;
import org.krystilize.colorise.queue.QueueSystem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class GameInstance extends SharedInstance {

    public static final Tag<Set<Point>> TOGGLED_BLOCKS = Tag.<Set<Point>>Transient("toggled_blocks").defaultValue(ConcurrentHashMap.newKeySet());

    protected final ColoriseGame game;
    protected final Player player1;
    protected final Player player2;

    public GameInstance(QueueSystem queue, GameInfo info) {
        super(UUID.randomUUID(), info.parent());
        this.game = new ColoriseGame(info.players(), queue);
        this.player1 = info.players().get(0);
        player1.setTag(Team.TAG, Team.BLUE);
        this.player2 = info.players().get(1);
        player2.setTag(Team.TAG, Team.GREEN);
    }

    protected abstract List<Mechanic> mechanics();
    private boolean started = false;
    public synchronized void start() {
        if (started) {
            return;
        }
        started = true;

        Map<Class<? extends Mechanic>, Mechanic> loadedMechanics = new HashMap<>();
        Mechanic.Context context = new MechanicsContextImpl(game, this.eventNode(), this, loadedMechanics);

        // load all mechanics
        for (Mechanic mechanic : this.mechanics()) {
            mechanic.setup(context);
            loadedMechanics.put(mechanic.getClass(), mechanic);
        }
    }

    public void stop() {

        if (!started) {
            throw new IllegalStateException("Cannot stop a game that has not started");
        }

        Map<Class<? extends Mechanic>, Mechanic> loadedMechanics = mechanics().stream()
                .collect(Collectors.toMap(Mechanic::getClass, m -> m));
        Mechanic.Context context = new MechanicsContextImpl(game, this.eventNode(), this, loadedMechanics);

        // unload all mechanics
        for (Mechanic mechanic : this.mechanics().reversed()) {
            mechanic.stop(context);
            loadedMechanics.remove(mechanic.getClass());
        }

        // send all players back to the queue
        if (player1.isOnline()) {
            game.queue().addPlayer(player1);
            player1.setInstance(game.queue().lobby(), Server.SPAWN);
        }
        if (player2.isOnline()) {
            game.queue().addPlayer(player2);
            player2.setInstance(game.queue().lobby(), Server.SPAWN);
        }
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public String getElapsedDisplay() {
        return Util.getFormattedTime(getElapsedMillis());
    }

    public long getElapsedMillis() {
        long ticks = this.getWorldAge();
        return ticks * 50;
    }

    private record MechanicsContextImpl(ColoriseGame game, EventNode<InstanceEvent> events, GameInstance instance,
                                        Map<Class<? extends Mechanic>, Mechanic> loadedMechanics) implements Mechanic.Context {

        @Override
        public <T extends Mechanic> T mechanic(Class<T> mechanic) {
            Mechanic loaded = loadedMechanics.get(mechanic);
            if (loaded == null) {
                throw new IllegalStateException("Mechanic " + mechanic.getSimpleName() + " not loaded");
            }
            return mechanic.cast(loaded);
        }
    }
}
