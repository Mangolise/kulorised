package net.mangolise.kulorised;

import net.hollowcube.polar.PolarWorld;
import net.mangolise.gamesdk.log.Log;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.mangolise.kulorised.colors.WorldAnalysis;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

/**
 * Analyzes the blocks in the instance for specific block types
 */
public class BlockAnalysis {

    // TODO: Make this work for more than one instance
    private static final Path pathToRegions = Path.of("worlds/level0/region");

    public static Analysis<Color> COLORED_BLOCKS = new Analysis<>();
    public static Analysis<Color> WINDOW_PANES = new Analysis<>();
    public static Analysis<Boolean> CHECKPOINT_PLATES = new Analysis<>();
    public static Analysis<Block> TERRACOTTA = new Analysis<>();
    public static Analysis<Boolean> WIN_PLATES = new Analysis<>();
    public static Analysis<Block> BUTTONS = new Analysis<>();

    public static void analyse(Instance instance, PolarWorld world) {
        long start = System.currentTimeMillis();
        COLORED_BLOCKS.blocks = WorldAnalysis.scanForColoredBlocks(world);
        WINDOW_PANES.blocks = WorldAnalysis.scanForWindowPanes(world);
        CHECKPOINT_PLATES.blocks = WorldAnalysis.scanForCheckpointPlates(world);
        TERRACOTTA.blocks = WorldAnalysis.scanForTerracottaBlocks(world);
        WIN_PLATES.blocks = WorldAnalysis.scanForWinPlates(world);
        BUTTONS.blocks = net.mangolise.gamesdk.instance.InstanceAnalysis.scanForBlocks(world, (b) -> b.compare(Block.STONE_BUTTON));

        long end = System.currentTimeMillis();
        Log.logger().info("Analysis took {}ms", end - start);

        // Switch buttons to levers
        BUTTONS.get().forEach((point, block) -> instance.setBlock(point, Block.LEVER.withProperty("facing", block.getProperty("facing")).withProperty("face", block.getProperty("face"))));
        // checkpoint plates, and win plates need to be removed
        CHECKPOINT_PLATES.get().forEach((point, ignored) -> instance.setBlock(point, Block.AIR));
        WIN_PLATES.get().forEach((point, ignored) -> instance.setBlock(point, Block.AIR));
        COLORED_BLOCKS.get().forEach((point, ignored) -> instance.setBlock(point, Block.AIR));
    }

    public static class Analysis<T> {

        private @Nullable Map<Point, T> blocks = null;

        public @NotNull Map<Point, T> get() {
            if (blocks == null) {
                throw new IllegalStateException("Cannot access analysis before it is complete");
            }
            return Collections.unmodifiableMap(blocks);
        }
    }
}
