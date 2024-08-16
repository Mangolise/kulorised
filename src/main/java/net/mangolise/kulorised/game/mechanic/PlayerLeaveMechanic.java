package net.mangolise.kulorised.game.mechanic;

import net.minestom.server.event.player.PlayerDisconnectEvent;

public class PlayerLeaveMechanic implements Mechanic {
    @Override
    public void setup(Context context) {
        context.events().addListener(PlayerDisconnectEvent.class, event -> context.instance().stop());
    }
}
