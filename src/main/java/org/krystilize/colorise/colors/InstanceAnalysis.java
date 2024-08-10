package org.krystilize.colorise.colors;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.palette.Palette;
import org.krystilize.colorise.Color;
import org.krystilize.colorise.game.ColoredBlocks;
import org.krystilize.colorise.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class InstanceAnalysis {

    public static ColoredBlocks scanForColoredBlocks(Instance instance, Path pathToRegions) {
        Map<Point, String> blocks = new ConcurrentHashMap<>();

        // for all regions
        try (var dir = Files.newDirectoryStream(pathToRegions)) {
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
                        futures.add(instance.loadChunk(x / 16, z / 16).thenAccept(chunk -> {
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
                                blocks.put(point, block.name());
                            });
                        }));
                    }
                }

                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

                long endTime = System.currentTimeMillis();

                System.out.println("Analysed region " + regionX + " " + regionZ + " in " + (endTime - startTime) + "ms");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<Point, Color> colors = new HashMap<>();

        blocks.forEach((point, blockName) -> {
            Color color = Color.fromBlockName(blockName);
            colors.put(point, color);
        });

        return new ColoredBlocks(instance, Map.copyOf(colors));
    }
}
