package org.krystilize.colorise.game.mechanic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;
import org.krystilize.colorise.Util;
import org.krystilize.colorise.game.GameInstance;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DoorControlMechanic implements Mechanic {

    @Override
    public void setup(Context context) {

        BlockAnalysisMechanic analysis = context.mechanic(BlockAnalysisMechanic.class);

        Map<Point, Block> terracotta = analysis.TERRACOTTA.get();

        context.events().addListener(PlayerBlockInteractEvent.class, event -> {
            Player player = event.getPlayer();
            GameInstance game = context.instance();
            Block block = event.getBlock();

            if (!Block.STONE_BUTTON.compare(block)) {
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

            for (var toggleEntry : togglePositions) {
                Point togglePosition = toggleEntry.getKey();
                Block toggleBlock = toggleEntry.getValue();
                Block current = game.getBlock(togglePosition);

                game.setBlock(togglePosition, current.isAir() ? toggleBlock : Block.AIR);
            }

            Util.playerAction(player, "", "Door toggled", controllerPos);
        });
    }
}
