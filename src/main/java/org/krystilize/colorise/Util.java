package org.krystilize.colorise;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import org.krystilize.colorise.game.ColoredBlocks;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class Util {

    public static void forEachNonAirBlockInChunk(Chunk chunk, BiConsumer<Point, Block> consumer) {
        // for each block in the chunk, check if it is air
        for (int blockX = chunk.getChunkX() * 16; blockX < chunk.getChunkX() * 16 + 16; blockX++) {
            for (int blockY = chunk.getMinSection() * 16; blockY < chunk.getMaxSection() * 16; blockY++) {
                for (int blockZ = chunk.getChunkZ() * 16; blockZ < chunk.getChunkZ() * 16 + 16; blockZ++) {
                    if (chunk.getBlock(blockX, blockY, blockZ).compare(Block.AIR)) {
                        continue;
                    }
                    consumer.accept(new Vec(blockX, blockY, blockZ), chunk.getBlock(blockX, blockY, blockZ));
                }
            }
        }
    }

    private static final IntSet concreteBlocks = IntSet.of(
            Block.values().stream()
                    .filter(block -> block.name().endsWith("concrete"))
                    .filter(block -> !block.name().contains("white"))
                    .filter(block -> !block.name().contains("gray"))
                    .filter(block -> !block.name().contains("black"))
                    .mapToInt(Block::stateId)
                    .toArray()
    );
    public static boolean isBlockColoredConcrete(Block block) {
        return concreteBlocks.contains(block.stateId());
    }

    public static void setPlayerGamemode(Player player, GameMode gamemode) {
        switch (gamemode) {
            case CREATIVE -> {
                player.setGameMode(GameMode.CREATIVE);
                player.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE).setBaseValue(4.5);
            }

            case ADVENTURE -> {
                player.setGameMode(GameMode.ADVENTURE);
                player.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE).setBaseValue(-1);
            }

            case SPECTATOR -> player.setGameMode(GameMode.SPECTATOR);
        }
    }

    public static final Set<String> ADMINS = Set.of("Calcilore", "EclipsedMango", "Krystilize", "CoPokBl");

    /**
     * This debug util method slowly rotates throughout all colors for all players in the instance
     */
    public static void DEBUG_slowlySwapColors(ColoredBlocks blocks) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> {
            for (boolean enabled : new boolean[]{true, false}) {
                for (Color color : Color.values()) {
                    for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {

                        if (!player.getInstance().equals(blocks.instance())) {
                            continue;
                        }

                        Util.log("Setting " + player.getUsername() + " to " + color + " " + enabled + "!");
                        blocks.setColor(enabled, player, color).join();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, 0, 1, TimeUnit.MILLISECONDS);
    }

    public static void log(Object log) {
        String logString = log.toString();
        Audiences.all().sendMessage(Component.text(logString));
    }
}
