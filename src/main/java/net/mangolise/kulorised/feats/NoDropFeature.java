package net.mangolise.kulorised.feats;

import net.mangolise.gamesdk.Game;
import net.mangolise.kulorised.KulorisedGame;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.item.ItemDropEvent;

public class NoDropFeature implements Game.Feature<KulorisedGame> {

    @Override
    public void setup(Context<KulorisedGame> context) {
        MinecraftServer.getGlobalEventHandler().addListener(ItemDropEvent.class, event -> event.setCancelled(true));
    }
}
