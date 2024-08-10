package org.krystilize.colorise.game.mechanic;

import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.InstanceEvent;
import org.krystilize.colorise.game.ColoriseGame;
import org.krystilize.colorise.game.GameInstance;

public interface Mechanic {
    default void setup(Context context) {
    }

    default void stop(Context context) {
    }

    interface Context {
        ColoriseGame game();
        EventNode<InstanceEvent> events();
        GameInstance instance();

        <T extends Mechanic> T mechanic(Class<T> mechanic);
    }
}
