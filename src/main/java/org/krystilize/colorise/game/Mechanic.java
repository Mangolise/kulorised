package org.krystilize.colorise.game;

import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.InstanceEvent;

public interface Mechanic {
    void setup(ColoriseGame game, EventNode<InstanceEvent> events, GameInstance instance);
}
