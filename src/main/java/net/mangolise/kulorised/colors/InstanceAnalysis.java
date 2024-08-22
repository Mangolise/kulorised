package net.mangolise.kulorised.colors;

import net.hollowcube.polar.PolarChunk;
import net.hollowcube.polar.PolarWorld;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.mangolise.kulorised.Color;
import net.mangolise.kulorised.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class InstanceAnalysis {

    public static Map<Point, Color> scanForColoredBlocks(Instance instance, PolarWorld world) {
        Map<Point, Block> blocks = scanForBlocks(instance, world, Util::isColoredBlock);

        Map<Point, Color> colors = new HashMap<>();

        blocks.forEach((point, block) -> {
            Color color = Color.fromBlockName(block.name());
            colors.put(point, color);
        });

        return Map.copyOf(colors);
    }

    public static Map<Point, Color> scanForWindowPanes(Instance instance, PolarWorld world) {
        Map<Point, Block> blocks = scanForBlocks(instance, world, Util::isWindowPane);

        Map<Point, Color> colors = new HashMap<>();

        blocks.forEach((point, block) -> {
            Color color = Color.fromBlockName(block.name());
            colors.put(point, color);
        });

        return Map.copyOf(colors);
    }

    public static Map<Point, Boolean> scanForCheckpointPlates(Instance instance, PolarWorld world) {
        Map<Point, Block> blocks = scanForBlocks(instance, world, Util::isCheckpointPlate);

        Map<Point, Boolean> plates = new HashMap<>();
        blocks.forEach((point, block) -> plates.put(point, block.equals(Block.LIGHT_WEIGHTED_PRESSURE_PLATE)));

        return Map.copyOf(plates);
    }

    public static Map<Point, Boolean> scanForWinPlates(Instance instance, PolarWorld world) {
        Map<Point, Block> blocks = scanForBlocks(instance, world, Util::isWinPlate);

        Map<Point, Boolean> plates = new HashMap<>();
        blocks.forEach((point, block) -> plates.put(point, false));

        return Map.copyOf(plates);
    }

    public static Map<Point, Block> scanForTerracottaBlocks(Instance instance, PolarWorld world) {
        return scanForBlocks(instance, world, Util::isTerracotta);
    }


    /**
     * Scans for blocks matching the given predicate.
     * <p>
     *     Note that this method will not scan for air blocks.
     * @param instance the instance to scan
     * @param blockPredicate the predicate to match
     * @return a map of points to blocks
     */
    public static Map<Point, Block> scanForBlocks(Instance instance, PolarWorld world, Predicate<Block> blockPredicate) {
        Map<Point, Block> blocks = new ConcurrentHashMap<>();

        // for all chunks
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (PolarChunk polarChunk : world.chunks()) {
            futures.add(instance.loadChunk(polarChunk.x(), polarChunk.z()).thenAccept(chunk -> {
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
                    if (!blockPredicate.test(block)) {
                        return;
                    }
                    blocks.put(point, block);
                });
            }));
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        long endTime = System.currentTimeMillis();

        System.out.println("Analyed instance in " + (endTime - startTime) + "ms");

        return Map.copyOf(blocks);
    }
}
