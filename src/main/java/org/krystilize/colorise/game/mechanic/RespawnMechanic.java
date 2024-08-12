package org.krystilize.colorise.game.mechanic;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.krystilize.colorise.Util;

/**
 * Makes players respawn when they die (fall into the void)
 */
public class RespawnMechanic implements Mechanic {

    @Override
    public void setup(Context context) {

        context.events().addListener(PlayerMoveEvent.class, event -> {
            Player player = event.getPlayer();
            Instance instance = event.getInstance();

            // respawn the player if they fall into the void
            if (event.getNewPosition().y() <= -10) {
                player.teleport(player.getRespawnPoint());
                return;
            }

            // Get the block that the player is standing on
            Block block = instance.getBlock(event.getNewPosition().sub(0, 0.1, 0));

            // respawn the player if they are standing on stained-glass
            if (Util.isStainedGlass(block)) {
                player.teleport(player.getRespawnPoint());
            }
        });
    }
}
