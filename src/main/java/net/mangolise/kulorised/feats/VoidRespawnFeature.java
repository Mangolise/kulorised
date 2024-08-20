package net.mangolise.kulorised.feats;

import net.mangolise.gamesdk.Game;
import net.mangolise.kulorised.KulorisedGame;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerMoveEvent;

public class VoidRespawnFeature implements Game.Feature<KulorisedGame> {

    @Override
    public void setup(Context<KulorisedGame> context) {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerMoveEvent.class, event -> {
            if (event.getPlayer().getPosition().y() < 0) {
                event.getPlayer().teleport(event.getPlayer().getRespawnPoint());
            }
        });
    }
}
