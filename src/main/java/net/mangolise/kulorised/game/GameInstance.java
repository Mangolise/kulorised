package net.mangolise.kulorised.game;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.mangolise.kulorised.Util;
import net.mangolise.kulorised.game.mechanic.Mechanic;
import net.mangolise.kulorised.queue.QueueSystem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("UnstableApiUsage")
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

        // Sound
        Sound sound = Sound.sound(SoundEvent.ENTITY_WITHER_SPAWN, Sound.Source.BLOCK, 0.7f, 1);
        player1.playSound(sound);
        player2.playSound(sound);
    }

    public void stop() {
        if (!started) {
            throw new IllegalStateException("Cannot stop a game that has not started");
        }

        broadcast(Component.text("Game has ended").color(TextColor.fromHexString("#eb4015")));

        // send all players back to the queue, or not
        game.queue().resetPlayer(player1);
        game.queue().resetPlayer(player2);
    }

    public void broadcast(Component msg) {
        player1.sendMessage(msg);
        player2.sendMessage(msg);
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
