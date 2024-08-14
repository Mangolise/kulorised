package org.krystilize.colorise.game.mechanic;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.utils.PacketUtils;
import org.krystilize.colorise.BlockAnalysis;
import org.krystilize.colorise.Util;
import org.krystilize.colorise.game.GameInstance;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DoorControlMechanic implements Mechanic {

    @Override
    public void setup(Context context) {
        Map<Point, Block> terracotta = BlockAnalysis.TERRACOTTA.get();

        context.events().addListener(PlayerBlockInteractEvent.class, event -> {
            GameInstance game = context.instance();

            if (event.getInstance() != game) {
                return;
            }

            Player player = event.getPlayer();
            Block block = event.getBlock();

            if (!Block.LEVER.compare(block) || player.isSneaking()) {
                return;
            }

//            Util.log("Player " + player.getUsername() + " interacted with " + event.getBlock() + " at " + event.getBlockPosition());

            Point pos = event.getBlockPosition();

            Point controllerPos = Util.neighbors(pos).stream()
                    .filter(blockPos -> Util.isTerracotta(game.getBlock(blockPos)))
                    .findAny()
                    .orElse(null);

            if (controllerPos == null) {
                return;
            }

            Block controller = game.getBlock(controllerPos);

            Set<Map.Entry<Point, Block>> togglePositions = terracotta.entrySet().stream()
                    .filter(entry -> controller.compare(entry.getValue()))
                    .filter(entry -> !entry.getKey().equals(controllerPos))
                    .collect(Collectors.toUnmodifiableSet());

            Set<Point> toggled = game.getTag(GameInstance.TOGGLED_BLOCKS);

            for (var toggleEntry : togglePositions) {
                Point togglePosition = toggleEntry.getKey();
                Block toggleBlock = toggleEntry.getValue();

                Block newBlock;
                if (!toggled.contains(togglePosition)) {
                    newBlock = Block.AIR;
                    toggled.add(togglePosition);
                } else {
                    newBlock = toggleBlock;
                    toggled.remove(togglePosition);
                }

                PacketUtils.sendGroupedPacket(game.getPlayers(), new BlockChangePacket(togglePosition, newBlock));
            }
            if (toggled.contains(event.getBlockPosition())) { // Lever itself
                toggled.remove(event.getBlockPosition());
                Block newBlock = event.getBlock().withProperty("powered", "false");
                BlockChangePacket packet = new BlockChangePacket(event.getBlockPosition(), newBlock);
                PacketUtils.sendGroupedPacket(game.getPlayers(), packet);
            } else {
                toggled.add(event.getBlockPosition());
                Block newBlock = event.getBlock().withProperty("powered", "true");
                BlockChangePacket packet = new BlockChangePacket(event.getBlockPosition(), newBlock);
                PacketUtils.sendGroupedPacket(game.getPlayers(), packet);
            }

            game.setTag(GameInstance.TOGGLED_BLOCKS, toggled);

            Util.playerAction(player, "", "Door toggled", controllerPos);
        });
    }
}
