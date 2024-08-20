package net.mangolise.kulorised.feats;

import net.mangolise.gamesdk.Game;
import net.mangolise.kulorised.KulorisedGame;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerMoveEvent;

// TODO: Put in game sdk as default feature
public class NoServerCrashFeature implements Game.Feature<KulorisedGame> {

    @Override
    public void setup(Context<KulorisedGame> context) {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerMoveEvent.class, event -> {
            if (event.getNewPosition().y() > 100_000) {
                event.setCancelled(true);
                event.getPlayer().teleport(event.getPlayer().getRespawnPoint());
            }
        });
    }
}
