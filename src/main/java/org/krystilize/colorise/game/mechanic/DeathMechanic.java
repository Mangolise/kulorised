package org.krystilize.colorise.game.mechanic;

import net.minestom.server.event.player.PlayerMoveEvent;

/**
 * Makes players respawn when they die (fall into the void)
 */
public class DeathMechanic implements Mechanic {

    @Override
    public void setup(Context context) {
        context.events().addListener(PlayerMoveEvent.class, e -> {
            if (e.getNewPosition().y() > -10) {
                return;
            }

            e.getPlayer().teleport(e.getPlayer().getRespawnPoint());
        });
    }
}
