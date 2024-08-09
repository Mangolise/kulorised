package org.krystilize.colorise;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.*;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.palette.Palette;
import org.krystilize.colorise.Commands.GameModeCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Server {

    public static void main(String[] args) {
        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();

        // Create the instance
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer lobbyInstance = instanceManager.createInstanceContainer(new AnvilLoader("worlds/lobby"));

        lobbyInstance.setChunkSupplier(LightingChunk::new);

        // for all regions
        try (var dir = Files.newDirectoryStream(Path.of("worlds/lobby/region"))) {
            for (Path regionName : dir) {
                String coords = regionName.getFileName().toString().replace("r.", "").replace(".mca", "");
                String[] split = coords.split("\\.");
                int regionX = Integer.parseInt(split[0]);
                int regionZ = Integer.parseInt(split[1]);

                // load all chunks in these regions (512 x 512 blocks)
                int startX = regionX * 512;
                int startZ = regionZ * 512;
                int endX = startX + 512;
                int endZ = startZ + 512;

                long startTime = System.currentTimeMillis();
                List<CompletableFuture<?>> futures = new ArrayList<>();

                for (int x = startX; x < endX; x += 16) {
                    for (int z = startZ; z < endZ; z += 16) {
                        futures.add(lobbyInstance.loadChunk(x / 16, z / 16).thenAccept(chunk -> {
                            DynamicChunk dynamicChunk = (DynamicChunk) chunk;

                            boolean isChunkEmpty = true;
                            for (Section section : dynamicChunk.getSections()) {
                                Palette blockPalette = section.blockPalette();
                                if (blockPalette.count() > 1) {
                                    isChunkEmpty = false;
                                    break;
                                }

                                if (blockPalette.count() == 1 && blockPalette.get(0, 0, 0) != 0){
                                    isChunkEmpty = false;
                                    break;
                                }
                            }

                            if (isChunkEmpty) {
                                return;
                            }

                            Util.forEachNonAirBlockInChunk(chunk, (point, block) -> {
                                if (!Util.isBlockColoredConcrete(block)) {
                                    return;
                                }
                                System.out.println("Block at " + point + " is " + block.name());
                            });
                        }));
                    }
                }

                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

                long endTime = System.currentTimeMillis();

                System.out.println("Loaded region " + regionX + " " + regionZ + " in " + (endTime - startTime) + "ms");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(lobbyInstance);
            player.setRespawnPoint(new Pos(0.5, 36, 0.5));
            Util.setPlayerGamemode(player, GameMode.ADVENTURE);
        });

        MinecraftServer.getCommandManager().register(new GameModeCommand());

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);
    }
}
