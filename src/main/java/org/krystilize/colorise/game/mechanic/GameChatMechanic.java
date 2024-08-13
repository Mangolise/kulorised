package org.krystilize.colorise.game.mechanic;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import org.krystilize.colorise.Util;
import org.krystilize.colorise.game.ColoriseGame;

public class GameChatMechanic implements Mechanic {

    @Override
    public void setup(Context context) {
        context.events().addListener(PlayerChatEvent.class, e -> {
            e.setCancelled(true);

            ColoriseGame game = context.game();

            Component message = Util.formatMessage("GAME", e.getPlayer(), e.getMessage());
            for (Player p : game.players()) {
                p.sendMessage(message);
            }
        });
    }
}
