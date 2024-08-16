package net.mangolise.kulorised.game.mechanic;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import org.jetbrains.annotations.UnknownNullability;
import net.mangolise.kulorised.BlockAnalysis;
import net.mangolise.kulorised.Color;
import net.mangolise.kulorised.game.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WindowsMechanic implements Mechanic {

    private @UnknownNullability Instance instance;
    private @UnknownNullability Map<Point, Color> blocks;
    @Override
    public void setup(Context context) {
        instance = context.instance();
        blocks = BlockAnalysis.WINDOW_PANES.get();

        context.events().addListener(PlayerTickEvent.class, event -> {
            long age = event.getInstance().getWorldAge();

            // Only update window blocks once every 20 ticks
            if (age % 20 != 0) {
                return;
            }

            Player player = event.getPlayer();
            Team team = player.getTag(Team.TAG);

            List<SendablePacket> packets = new ArrayList<>();
            blocks.forEach((point, color) -> {
                Block block = team.colors().contains(color) ? color.block() : Block.AIR;
                packets.add(new BlockChangePacket(point, block));
            });

            player.sendPackets(packets.toArray(SendablePacket[]::new));
        });
    }
}
