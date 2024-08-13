package org.krystilize.colorise.game.mechanic;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import org.krystilize.colorise.game.GameInstance;

import java.time.Duration;
import java.util.*;

public class WinMechanic implements Mechanic {
    @Override
    public void setup(Context context) {
        GameInstance instance = context.instance();

        instance.scheduler().scheduleTask(() -> {
            doSetup(instance, context);
            return TaskSchedule.stop();
        }, TaskSchedule.tick(10));
    }

    private void doSetup(GameInstance instance, Context context) {
        Map<Point, Boolean> winPlates = context.mechanic(BlockAnalysisMechanic.class).WIN_PLATES.get();
        Set<UUID> wonPlayers = Collections.synchronizedSet(new HashSet<>());
        Set<SendablePacket> packets = new HashSet<>();

        for (Point platePos : winPlates.keySet()) {
            packets.add(new BlockChangePacket(platePos, Block.AIR));
        }

        instance.getPlayer1().sendPackets(packets);
        instance.getPlayer2().sendPackets(packets);

        context.events().addListener(PlayerMoveEvent.class, (event) -> {
            if (wonPlayers.size() >= 2) {
                return;
            }

            Point pos = event.getNewPosition();

            for (Point platePos : winPlates.keySet()) {
                // if player is within the win
                if (platePos.blockX() == pos.blockX() && platePos.blockZ() == pos.blockZ() &&
                        (platePos.blockY() == pos.blockY() || platePos.blockY() + 1 == pos.blockY())) {
                    synchronized (wonPlayers) {
                        wonPlayers.add(event.getPlayer().getUuid());

                        if (wonPlayers.size() == 2) {
                            doWin(instance.getPlayer1());
                            doWin(instance.getPlayer2());

                            MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                                instance.stop();
                                return TaskSchedule.stop();
                            }, TaskSchedule.seconds(10));
                        }
                    }
                }
            }
        });
    }

    private void doWin(Player player) {
        Title.Times times = Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(300), Duration.ofMillis(100));
        player.showTitle(Title.title(Component.text("You Won!"), Component.empty(), times));
        Sound sound = Sound.sound(SoundEvent.ENTITY_ENDER_DRAGON_DEATH, Sound.Source.BLOCK, 0.7f, 1);

        player.playSound(sound);
    }
}
