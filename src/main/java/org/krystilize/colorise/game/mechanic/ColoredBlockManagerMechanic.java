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
import org.krystilize.colorise.entity.BlockOutlineEntityGroup;
import org.krystilize.colorise.event.ColorChangeEvent;
import org.krystilize.colorise.entity.BlockOutlineEntity;
import org.krystilize.colorise.game.GameInstance;
import org.krystilize.colorise.game.Team;

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
        blocks = context.mechanic(BlockAnalysisMechanic.class).COLORED_BLOCKS.get();

        Util.log("ColoredBlocks created with " + blocks.size() + " blocks.");
        Map<Point, BlockOutlineEntityGroup> displayEntities = instance.getTag(INSTANCE_DISPLAY_ENTITIES);

        instance.scheduleNextTick(ignored -> {
            for (var entry : blocks.entrySet()) {
                BlockOutlineEntity normal = createBlockOutlineDisplayEntity(entry, false);
                BlockOutlineEntity glowing = createBlockOutlineDisplayEntity(entry, true);

                displayEntities.put(entry.getKey(), new BlockOutlineEntityGroup(normal, glowing));
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

    public BlockOutlineEntity createBlockOutlineDisplayEntity(Map.Entry<Point, Color> entry, boolean glowing) {
        BlockOutlineEntity entity = new BlockOutlineEntity(entry.getValue());
        entity.setInstance(instance, entry.getKey());
        entity.setGlowing(glowing);

        entity.updateViewableRule(player -> {
            boolean teammateColorOff = player.getTag(PLAYER_SELECTED_COLORS).contains(entry.getValue());
            boolean playerColorOff = (player.getTag(Team.TAG) == Team.BLUE ?
                    ((GameInstance)instance).getPlayer2() : ((GameInstance)instance).getPlayer1())
                    .getTag(PLAYER_SELECTED_COLORS).contains(entry.getValue());

            boolean sameTeam = player.getTag(Team.TAG) == Team.getTeamFromColor(entry.getValue());

            if (sameTeam) {
//                Util.log(player.getUsername() + ": " + entry.getValue() + " " + playerColorOff);
                return playerColorOff != glowing;
            }

            return teammateColorOff && !glowing;
        });

        return entity;
    }

    private static final Tag<Map<Point, BlockOutlineEntityGroup>> INSTANCE_DISPLAY_ENTITIES = Tag.<Map<Point, BlockOutlineEntityGroup>>Transient("display_entities")
            .defaultValue(ConcurrentHashMap::new);

    private static final Tag<Set<Color>> PLAYER_SELECTED_COLORS = Tag.<Set<Color>>Transient("selected_colors")
            .defaultValue(ConcurrentHashMap::newKeySet);

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private void updateEntities() {
        for (BlockOutlineEntityGroup blockOutlineDisplayEntity : instance.getTag(INSTANCE_DISPLAY_ENTITIES).values()) {
            blockOutlineDisplayEntity.glowingEntity().updateViewableRule();
            blockOutlineDisplayEntity.normalEntity().updateViewableRule();
        }
    }

    private CompletableFuture<Void> setColor(boolean enabled, Player player, Color color) {
        return CompletableFuture.runAsync(() -> {
//            Util.log("Setting color " + color + " to " + enabled + " for " + player.getUsername() + ".");

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

                player.sendPackets(packets.toArray(SendablePacket[]::new));
            } else {
                // Remove the blocks
                for (Point point : points) {
                    packets.add(new BlockChangePacket(point, Block.AIR.stateId()));
                }

                // add the color from the set
                selectedColors.add(color);

                instance.scheduler().scheduleNextTick(() -> {
                    player.sendPackets(packets.toArray(SendablePacket[]::new));
                });
            }

            player.setTag(PLAYER_SELECTED_COLORS, selectedColors);

            // update entities
            updateEntities();
        }, executor);
    }
}
