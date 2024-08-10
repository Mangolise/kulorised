package org.krystilize.colorise;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.*;
import net.minestom.server.instance.anvil.AnvilLoader;
import org.krystilize.colorise.commands.GameModeCommand;
import org.krystilize.colorise.colors.InstanceAnalysis;
import org.krystilize.colorise.game.ColoredBlocks;
import org.krystilize.colorise.game.ColoriseGame;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {

    public static void main(String[] args) {
        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();

        // Create the instance
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer lobbyInstance = instanceManager.createInstanceContainer(new AnvilLoader("worlds/lobby"));
        lobbyInstance.setChunkSupplier(LightingChunk::new);

        InstanceContainer level0Instance = instanceManager.createInstanceContainer(new AnvilLoader("worlds/level0"));
        ColoredBlocks coloredBlocks = InstanceAnalysis.scanForColoredBlocks(level0Instance, Path.of("worlds/level0/region"));
        ColoriseGame game = new ColoriseGame(level0Instance, coloredBlocks);
        game.start();
        System.out.println(game);

//        Util.DEBUG_slowlySwapColors(coloredBlocks);

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(level0Instance);
            player.setRespawnPoint(new Pos(0.5, 36, 0.5));
            Util.setPlayerGamemode(player, GameMode.ADVENTURE);

            if (Util.ADMINS.contains(player.getUsername())) {
                player.setPermissionLevel(4);
            }
        });

//        globalEventHandler.addListener(PlayerPacketOutEvent.class, event -> {
//            ServerPacket packet = event.getPacket();
//            switch (packet) {
//                case RegistryDataPacket registryDataPacket -> {}
//                case ChunkDataPacket chunkDataPacket -> {}
//                default -> {
//                    System.out.println(event.getPacket());
//                }
//            }
//        });

        MinecraftServer.getCommandManager().register(new GameModeCommand());

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);
    }
}
