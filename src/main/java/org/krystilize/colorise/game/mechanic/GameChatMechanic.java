package org.krystilize.colorise.game.mechanic;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import org.krystilize.colorise.game.ColoriseGame;

public class GameChatMechanic implements Mechanic {

    @Override
    public void setup(Context context) {
        context.events().addListener(PlayerChatEvent.class, e -> {
            e.setCancelled(true);

            ColoriseGame game = context.game();

            String msg = "[GAME] " + e.getPlayer().getUsername() + ": " + e.getMessage();
            for (Player p : game.players()) {
                p.sendMessage(msg);
            }
        });
    }
}
