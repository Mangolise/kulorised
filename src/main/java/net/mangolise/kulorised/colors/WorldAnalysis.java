package net.mangolise.kulorised.colors;

import net.hollowcube.polar.PolarWorld;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.mangolise.kulorised.Color;
import net.mangolise.kulorised.Util;

import java.util.*;

import static net.mangolise.gamesdk.instance.InstanceAnalysis.scanForBlocks;

public class WorldAnalysis {

    public static Map<Point, Color> scanForColoredBlocks(PolarWorld world) {
        Map<Point, Block> blocks = scanForBlocks(world, Util::isColoredBlock);

        Map<Point, Color> colors = new HashMap<>();

        blocks.forEach((point, block) -> {
            Color color = Color.fromBlockName(block.name());
            colors.put(point, color);
        });

        return Map.copyOf(colors);
    }

    public static Map<Point, Color> scanForWindowPanes(PolarWorld world) {
        Map<Point, Block> blocks = scanForBlocks(world, Util::isWindowPane);

        Map<Point, Color> colors = new HashMap<>();

        blocks.forEach((point, block) -> {
            Color color = Color.fromBlockName(block.name());
            colors.put(point, color);
        });

        return Map.copyOf(colors);
    }

    public static Map<Point, Boolean> scanForCheckpointPlates(PolarWorld world) {
        Map<Point, Block> blocks = scanForBlocks(world, Util::isCheckpointPlate);

        Map<Point, Boolean> plates = new HashMap<>();
        blocks.forEach((point, block) -> plates.put(point, block.equals(Block.LIGHT_WEIGHTED_PRESSURE_PLATE)));

        return Map.copyOf(plates);
    }

    public static Map<Point, Boolean> scanForWinPlates(PolarWorld world) {
        Map<Point, Block> blocks = scanForBlocks(world, Util::isWinPlate);

        Map<Point, Boolean> plates = new HashMap<>();
        blocks.forEach((point, block) -> plates.put(point, false));

        return Map.copyOf(plates);
    }

    public static Map<Point, Block> scanForTerracottaBlocks(PolarWorld world) {
        return scanForBlocks(world, Util::isTerracotta);
    }
}
