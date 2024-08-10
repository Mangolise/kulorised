package org.krystilize.colorise.game;

import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.trait.InstanceEvent;

/**
 * Makes players respawn when they die (fall into the void)
 */
public class DeathMechanic implements Mechanic {

    @Override
    public void setup(ColoriseGame game, EventNode<InstanceEvent> events, GameInstance instance) {
        events.addListener(PlayerMoveEvent.class, e -> {
            if (e.getNewPosition().y() > -10) {
                return;
            }

            e.getPlayer().teleport(e.getPlayer().getRespawnPoint());
        });
    }
}
