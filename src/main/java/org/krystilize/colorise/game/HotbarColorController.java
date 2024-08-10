package org.krystilize.colorise.game;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientHeldItemChangePacket;
import org.jetbrains.annotations.Nullable;
import org.krystilize.colorise.Color;
import org.krystilize.colorise.Util;

/**
 * Controls setting colors when a player changes their hotbar selection
 */
public class HotbarColorController implements Mechanic {

    @Override
    public void setup(ColoriseGame game, EventNode<InstanceEvent> events, GameInstance instance) {

        // we need to update the player's hotbar whenever they spawn
        events.addListener(PlayerPacketEvent.class, event -> {
            Player player = event.getPlayer();

            if (!(event.getPacket() instanceof ClientHeldItemChangePacket changeHeldItemPacket)) {
                return;
            }

            // only change players in the game
            if (!game.players().contains(event.getPlayer())) {
                return;
            }

            ItemStack previous = player.getInventory().getItemStack(player.getHeldSlot());
            Color previousColor = Color.fromMaterial(previous.material());

            ItemStack current = player.getInventory().getItemStack(changeHeldItemPacket.slot());
            Color newColor = Color.fromMaterial(current.material());

            Util.log("Changing color from " + previousColor + " to " + newColor + " for " + player.getUsername() + ".");

            instance.scheduleNextTick(ignored -> {
                // TODO: Remove the assumption that we only have two players
                Player other = game.players().stream().filter(p -> p != player).findAny().orElseThrow();
                this.updateHotbar(game, player, other);
            });
        });
    }

    public void updateHotbar(ColoriseGame game, Player player, Player target) {
        ItemStack heldItem = player.getInventory().getItemStack(player.getHeldSlot());
        @Nullable Color heldColor = Color.fromMaterial(heldItem.material());

        for (Color color : Color.values()) {
            game.blocks().setColor(color == heldColor, target, color);
        }
    }
}
