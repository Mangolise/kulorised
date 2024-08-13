package org.krystilize.colorise.game.mechanic;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import org.krystilize.colorise.Util;
import org.krystilize.colorise.game.GameInstance;
import org.krystilize.colorise.leaderboard.LeaderboardManager;

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
                            doWin(instance);

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

    private void doWin(GameInstance game) {
        Title.Times times = Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(5000), Duration.ofMillis(100));
        Component timeTaken = Component.text("You took " + game.getElapsedDisplay() + " to win the game");
        Sound sound = Sound.sound(SoundEvent.ENTITY_ENDER_DRAGON_DEATH, Sound.Source.BLOCK, 0.4f, 1);

        for (Player player : game.getPlayers()) {
            player.showTitle(Title.title(Component.text("You Won!"), timeTaken, times));
            player.playSound(sound);
        }

        String playersString = game.getPlayer1().getUsername() + " and " + game.getPlayer2().getUsername();
        Util.broadcast(Component.text(playersString + " beat the game in " + game.getElapsedDisplay()).color(TextColor.fromHexString("#ffea00")));

        boolean gotRecord = LeaderboardManager.addCompletionPlayers(game.getPlayers(), game.getElapsedMillis());
        if (gotRecord) {
            Util.broadcast(Component.text("New record!").color(TextColor.fromHexString("#ffea00")));
            Util.sendNotification(game.getPlayers(), "New record!", NamedTextColor.GOLD, FrameType.CHALLENGE, Material.GOLD_BLOCK);
        }
    }
}
