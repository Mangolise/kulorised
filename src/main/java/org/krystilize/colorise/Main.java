package org.krystilize.colorise;

import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args) {
        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();

        // Create the instance
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

//        DynamicRegistry<Biome> biomeRegistry = MinecraftServer.getBiomeRegistry();
//        List<Map.Entry<String, Biome>> biomes = ColorBiomes.generateColorBiomes();
//        List<DynamicRegistry.Key<Biome>> biomeKeys = biomes.stream()
//                .map(entry -> biomeRegistry.register(entry.getKey(), entry.getValue())).toList();

        instanceContainer.setChunkSupplier(LightingChunk::new);

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 42, 0));
            player.setGameMode(GameMode.CREATIVE);

//            try {
//                player.sendResourcePacks(ResourcePackRequest.resourcePackRequest()
//                        .packs(ResourcePackInfo.resourcePackInfo()
//                                .uri(new URI("http://100.80.15.64:8001"))
//                                .id(UUID.randomUUID())
//                                .hash(UUID.randomUUID().toString())
//                                .build())
//                        .required(true)
//                        .build());
//            } catch (URISyntaxException e) {
//                throw new RuntimeException(e);
//            }

            player.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE).setBaseValue(100);
        });

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);
    }
}
