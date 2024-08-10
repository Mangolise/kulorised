package org.krystilize.colorise.game;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.tag.Tag;
import org.krystilize.colorise.Color;
import org.krystilize.colorise.entity.BlockOutlineDisplayEntity;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public record ColoredBlocks(Instance instance, Map<Point, Color> blocks) {

    private static final Tag<Map<Point, Entity>> PLAYER_DISPLAY_ENTITIES = Tag.<Map<Point, Entity>>Transient("display_entities")
            .defaultValue(ConcurrentHashMap::new);

    private static final Tag<Set<Color>> PLAYER_SELECTED_COLORS = Tag.<Set<Color>>Transient("selected_colors")
            .defaultValue(ConcurrentHashMap::newKeySet);

    public CompletableFuture<Void> setColor(boolean enabled, Player player, Color color) {

        // Get all block positions of the specified color
        Set<Point> points = blocks.entrySet().stream()
                .filter(entry -> entry.getValue().equals(color))
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());

        Map<Point, Entity> outlineEntities = player.getTag(PLAYER_DISPLAY_ENTITIES);
        Set<Color> selectedColors = player.getTag(PLAYER_SELECTED_COLORS);

        if (enabled) {
            // Set the blocks
            Set<BlockChangePacket> packets = new HashSet<>();
            for (Point point : points) {
                packets.add(new BlockChangePacket(point, color.block().stateId()));
            }
            player.sendPackets(packets.toArray(BlockChangePacket[]::new));

            // despawn all entities that are in the set
            for (Point point : points) {
                Entity entity = outlineEntities.get(point);
                if (entity != null) {
                    entity.removeViewer(player);
                    entity.remove();
                    outlineEntities.remove(point);
                }
            }

            // remove the color from the set
            selectedColors.remove(color);

            player.setTag(PLAYER_DISPLAY_ENTITIES, outlineEntities);
            player.setTag(PLAYER_SELECTED_COLORS, selectedColors);

            return CompletableFuture.completedFuture(null);
        } else {

            // spawn all entities that are in the set
            List<CompletableFuture<?>> spawningFutures = new ArrayList<>();
            for (Point point : points) {
                Entity entity = new BlockOutlineDisplayEntity(color);

                // Make it so that only this player can see this entity
                entity.setAutoViewable(false);
                spawningFutures.add(entity.setInstance(instance, point).thenRun(() -> entity.addViewer(player)));
                outlineEntities.put(point, entity);
            }

            // Remove the blocks after all entities have been spawned
            return CompletableFuture.allOf(spawningFutures.toArray(CompletableFuture[]::new)).thenRun(() -> {
                Set<BlockChangePacket> packets = new HashSet<>();
                for (Point point : points) {
                    packets.add(new BlockChangePacket(point, Block.AIR.stateId()));
                }
                player.sendPackets(packets.toArray(BlockChangePacket[]::new));
            }).thenRun(() -> {
                // remove the color from the set
                selectedColors.add(color);

                player.setTag(PLAYER_DISPLAY_ENTITIES, outlineEntities);
                player.setTag(PLAYER_SELECTED_COLORS, selectedColors);
            });
        }
    }
}
