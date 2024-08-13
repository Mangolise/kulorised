package org.krystilize.colorise;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.krystilize.colorise.colors.InstanceAnalysis;

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

    public static void analyse(Instance instance) {
        COLORED_BLOCKS.blocks = InstanceAnalysis.scanForColoredBlocks(instance, pathToRegions);
        WINDOW_PANES.blocks = InstanceAnalysis.scanForWindowPanes(instance, pathToRegions);
        CHECKPOINT_PLATES.blocks = InstanceAnalysis.scanForCheckpointPlates(instance, pathToRegions);
        TERRACOTTA.blocks = InstanceAnalysis.scanForTerracottaBlocks(instance, pathToRegions);
        WIN_PLATES.blocks = InstanceAnalysis.scanForWinPlates(instance, pathToRegions);

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
