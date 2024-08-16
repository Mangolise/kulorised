package net.mangolise.kulorised.game.mechanic;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.mangolise.kulorised.Util;
import net.mangolise.kulorised.game.ColoriseGame;

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
