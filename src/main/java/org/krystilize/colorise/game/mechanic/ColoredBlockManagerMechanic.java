package org.krystilize.colorise.game.mechanic;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.UnknownNullability;
import org.krystilize.colorise.Color;
import org.krystilize.colorise.Util;
import org.krystilize.colorise.colors.ColorChangeEvent;
import org.krystilize.colorise.entity.BlockOutlineDisplayEntity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ColoredBlockManagerMechanic implements Mechanic {

    public ColoredBlockManagerMechanic() {
    }

    private @UnknownNullability Instance instance;
    private @UnknownNullability Map<Point, Color> blocks;
    @Override
    public void setup(Context context) {
        instance = context.instance();
        blocks = context.mechanic(BlockAnalysisMechanic.class).getColoredBlocks();

        Util.log("ColoredBlocks created with " + blocks.size() + " blocks.");
        Map<Point, BlockOutlineDisplayEntity> displayEntities = instance.getTag(INSTANCE_DISPLAY_ENTITIES);

        instance.scheduleNextTick(ignored -> {
            for (var entry : blocks.entrySet()) {
                BlockOutlineDisplayEntity entity = new BlockOutlineDisplayEntity(entry.getValue());
                entity.updateViewableRule(viewer -> viewer.getTag(PLAYER_SELECTED_COLORS).contains(entry.getValue()));
                entity.setInstance(instance, entry.getKey());
                displayEntities.put(entry.getKey(), entity);
            }
        });

        instance.setTag(INSTANCE_DISPLAY_ENTITIES, displayEntities);

        context.events().addListener(ColorChangeEvent.class, event -> {

            // TODO: Remove the two player assumption
            // We assume there are only two players in the instance
            Player other = instance.getPlayers().stream()
                    .filter(player -> !player.equals(event.getPlayer()))
                    .findFirst()
                    .orElseThrow();

            for (Color color : Color.values()) {
                this.setColor(color == event.getColor(), other, color);
            }
        });
    }

    private static final Tag<Map<Point, BlockOutlineDisplayEntity>> INSTANCE_DISPLAY_ENTITIES = Tag.<Map<Point, BlockOutlineDisplayEntity>>Transient("display_entities")
            .defaultValue(ConcurrentHashMap::new);

    private static final Tag<Set<Color>> PLAYER_SELECTED_COLORS = Tag.<Set<Color>>Transient("selected_colors")
            .defaultValue(ConcurrentHashMap::newKeySet);

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private void updateEntities() {
        for (BlockOutlineDisplayEntity blockOutlineDisplayEntity : instance.getTag(INSTANCE_DISPLAY_ENTITIES).values()) {
            blockOutlineDisplayEntity.updateViewableRule();
        }
    }

    private CompletableFuture<Void> setColor(boolean enabled, Player player, Color color) {;
        return CompletableFuture.runAsync(() -> {
            Util.log("Setting color " + color + " to " + enabled + " for " + player.getUsername() + ".");

            // Get all block positions of the specified color
            Set<Point> points = blocks.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(color))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toUnmodifiableSet());

            Set<Color> selectedColors = player.getTag(PLAYER_SELECTED_COLORS);

            Set<SendablePacket> packets = new HashSet<>();

            if (enabled) {
                // Set the blocks
                for (Point point : points) {
                    packets.add(new BlockChangePacket(point, color.block().stateId()));
                }

                // remove the color from the set
                selectedColors.remove(color);
            } else {
                // Remove the blocks
                for (Point point : points) {
                    packets.add(new BlockChangePacket(point, Block.AIR.stateId()));
                }

                // add the color from the set
                selectedColors.add(color);
            }

            player.sendPackets(packets.toArray(SendablePacket[]::new));

            player.setTag(PLAYER_SELECTED_COLORS, selectedColors);

            // update entities
            updateEntities();
        }, executor);
    }
}
