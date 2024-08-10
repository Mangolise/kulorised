package org.krystilize.colorise.game;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.tag.Tag;
import org.krystilize.colorise.Color;
import org.krystilize.colorise.Util;
import org.krystilize.colorise.entity.BlockOutlineDisplayEntity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public record ColoredBlocks(Instance instance, Map<Point, Color> blocks) {

    public ColoredBlocks {
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

    public CompletableFuture<Void> setColor(boolean enabled, Player player, Color color) {;
        return CompletableFuture.runAsync(() -> {
//            Util.log("Setting color " + color + " to " + enabled + " for " + player.getUsername() + ".");

            // Get all block positions of the specified color
            Set<Point> points = blocks.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(color))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toUnmodifiableSet());

            Map<Point, BlockOutlineDisplayEntity> instanceDisplayEntities = instance.getTag(INSTANCE_DISPLAY_ENTITIES);
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

                // remove the color from the set
                selectedColors.add(color);
            }

            player.sendPackets(packets.toArray(SendablePacket[]::new));

            player.setTag(PLAYER_SELECTED_COLORS, selectedColors);

            // update entities
            updateEntities();
        }, executor);
    }
}
