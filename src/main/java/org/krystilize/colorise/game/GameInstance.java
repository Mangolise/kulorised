package org.krystilize.colorise.game;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.SharedInstance;
import org.krystilize.colorise.game.mechanic.Mechanic;
import org.krystilize.colorise.queue.QueueSystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class GameInstance extends SharedInstance {

    protected final ColoriseGame game;
    protected final Player player1;
    protected final Player player2;

    public GameInstance(QueueSystem queue, GameInfo info) {
        super(UUID.randomUUID(), info.parent());
        this.game = new ColoriseGame(info.players(), queue);
        this.player1 = info.players().get(0);
        this.player2 = info.players().get(1);
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
            player1.setInstance(game.queue().lobby());
        }
        if (player2.isOnline()) {
            game.queue().addPlayer(player2);
            player2.setInstance(game.queue().lobby());
        }
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
