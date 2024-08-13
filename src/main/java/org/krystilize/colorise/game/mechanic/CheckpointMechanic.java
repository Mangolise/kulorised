package org.krystilize.colorise.game.mechanic;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.UnknownNullability;
import org.krystilize.colorise.Util;
import org.krystilize.colorise.game.GameInstance;

public class CheckpointMechanic implements Mechanic {
    private @UnknownNullability Map<Point, Point> checkpoints;

    public void setup(Context context) {
        GameInstance instance = context.instance();

        instance.scheduler().scheduleTask(() -> {
            doSetup(instance, context);
            return TaskSchedule.stop();
        }, TaskSchedule.tick(10));
    }

    private void doSetup(GameInstance instance, Context context) {
        checkpoints = new ConcurrentHashMap<>();

        Map<Point, Boolean> pressurePlates = context.mechanic(BlockAnalysisMechanic.class).CHECKPOINT_PLATES.get();

        List<Point> spawnPlates = new ArrayList<>();
        Set<SendablePacket> packets = new HashSet<>();

        // get spawn points
        for (Entry<Point, Boolean> plate : pressurePlates.entrySet()) {
            if (plate.getValue()) {
                spawnPlates.add(plate.getKey());
                checkpoints.put(plate.getKey(), plate.getKey().add(0.5, 0.0, 0.5));

                packets.add(new BlockChangePacket(plate.getKey(), Block.AIR));
            }
        }

        // get checkpoint areas
        for (Entry<Point, Boolean> plate : pressurePlates.entrySet()) {
            if (plate.getValue()) {
                continue;
            }

            double closestDistance = 9999999999d;
            Point closestSpawn = Vec.ZERO;

            for (Point spawn : spawnPlates) {
                double distance = spawn.distance(plate.getKey());

                if (distance < closestDistance) {
                    closestSpawn = spawn;
                    closestDistance = distance;
                }
            }

            checkpoints.put(plate.getKey(), closestSpawn.add(0.5, 0.0, 0.5));
            packets.add(new BlockChangePacket(plate.getKey(), Block.AIR));
        }

        instance.getPlayer1().sendPackets(packets);
        instance.getPlayer2().sendPackets(packets);

        context.events().addListener(PlayerMoveEvent.class, (event) -> {
            Player player = event.getPlayer();
            Point pos = event.getNewPosition();

            for (Entry<Point, Point> plate : checkpoints.entrySet()) {
                Point platePos = plate.getKey();

                // if player is within the block checkpoint
                if     (platePos.blockX() == pos.blockX() && platePos.blockZ() == pos.blockZ() &&
                       (platePos.blockY() == pos.blockY() || platePos.blockY() + 1 == pos.blockY())) {

                    // if the current checkpoint is not this one
                    if (player.getRespawnPoint().distanceSquared(plate.getValue()) > 0.5) {
                        player.setRespawnPoint(Pos.fromPoint(plate.getValue()).withYaw(-90.0F));
                        Util.playerAction(player, "", "Checkpoint Reached!", player.getPosition());
                    }
                    break;
                }
            }
        });
    }
}
