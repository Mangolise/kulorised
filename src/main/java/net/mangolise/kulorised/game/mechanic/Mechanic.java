package net.mangolise.kulorised.game.mechanic;

import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.InstanceEvent;
import net.mangolise.kulorised.game.ColoriseGame;
import net.mangolise.kulorised.game.GameInstance;

public interface Mechanic {
    default void setup(Context context) {
    }

    interface Context {
        ColoriseGame game();
        EventNode<InstanceEvent> events();
        GameInstance instance();

        <T extends Mechanic> T mechanic(Class<T> mechanic);
    }
}
