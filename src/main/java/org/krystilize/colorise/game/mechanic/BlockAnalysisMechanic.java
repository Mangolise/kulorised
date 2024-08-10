package org.krystilize.colorise.game.mechanic;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.krystilize.colorise.Color;
import org.krystilize.colorise.colors.InstanceAnalysis;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

/**
 * Analyzes the blocks in the instance for specific block types
 */
public class BlockAnalysisMechanic implements Mechanic {
    private final Path pathToRegions;
    public BlockAnalysisMechanic(Path pathToRegions) {
        this.pathToRegions = pathToRegions;
    }


    public Analysis COLORED_BLOCKS = new Analysis();
    public Analysis WINDOW_PANES = new Analysis();


    @Override
    public void setup(Context context) {
        COLORED_BLOCKS.blocks = InstanceAnalysis.scanForColoredBlocks(context.instance(), pathToRegions);
        WINDOW_PANES.blocks = InstanceAnalysis.scanForWindowPanes(context.instance(), pathToRegions);
    }

    public static class Analysis {

        private @Nullable Map<Point, Color> blocks = null;

        public @NotNull Map<Point, Color> get() {
            if (blocks == null) {
                throw new IllegalStateException("Cannot access analysis before it is complete");
            }
            return Collections.unmodifiableMap(blocks);
        }
    }
}
