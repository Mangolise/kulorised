package org.krystilize.colorise;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;

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
                    .mapToInt(Block::stateId)
                    .toArray()
    );
    public static boolean isBlockColoredConcrete(Block block) {
        return concreteBlocks.contains(block.stateId());
    }
}
