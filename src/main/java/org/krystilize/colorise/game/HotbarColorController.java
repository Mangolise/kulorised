package org.krystilize.colorise.game;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientHeldItemChangePacket;
import org.krystilize.colorise.Color;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Controls setting colors when a player changes their hotbar selection
 * @param game
 */
public record HotbarColorController(ColoriseGame game) {

    public HotbarColorController {

        AtomicReference<CompletableFuture<Void>> waitingFuture = new AtomicReference<>(CompletableFuture.completedFuture(null));

        game.instance().eventNode().addListener(PlayerPacketEvent.class, event -> {
            Player player = event.getPlayer();

            if (!(event.getPacket() instanceof ClientHeldItemChangePacket changeHeldItemPacket)) {
                return;
            }

            // Wait for previous future
            waitingFuture.updateAndGet(future -> future.thenRun(() -> {
                ItemStack previous = player.getInventory().getItemStack(player.getHeldSlot());
                Color previousColor = Color.fromMaterial(previous.material());
                CompletableFuture<Void> previousFuture = previousColor == null ? CompletableFuture.completedFuture(null) :
                        game.blocks().setColor(false, player, previousColor);

                ItemStack current = player.getInventory().getItemStack(changeHeldItemPacket.slot());
                Color newColor = Color.fromMaterial(current.material());
                CompletableFuture<Void> currentFuture = newColor == null ? CompletableFuture.completedFuture(null) :
                        game.blocks().setColor(true, player, newColor);

                waitingFuture.set(CompletableFuture.allOf(previousFuture, currentFuture));
            }));
        });
    }
}
