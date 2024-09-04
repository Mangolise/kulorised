package net.mangolise.kulorised;

import net.mangolise.gamesdk.limbo.Limbo;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.bungee.BungeeCordProxy;

public class Test {
    private static final KulorisedConfig TEST_CONFIG = new KulorisedConfig();

    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        Limbo.waitForPlayers(2)
                .thenAccept(players -> {
                    KulorisedGame game = new KulorisedGame(TEST_CONFIG);
                    game.setup();
                });

        if (GameSdkUtils.useBungeeCord()) {
            BungeeCordProxy.enable();
        }

        server.start("0.0.0.0", GameSdkUtils.getConfiguredPort());
    }
}
