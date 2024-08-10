package org.krystilize.colorise.game;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientHeldItemChangePacket;
import net.minestom.server.network.packet.server.SendablePacket;
import org.jetbrains.annotations.Nullable;
import org.krystilize.colorise.Color;
import org.krystilize.colorise.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Controls setting colors when a player changes their hotbar selection
 * @param game
 */
public record HotbarColorController(ColoriseGame game) {

    public HotbarColorController {

        EventNode<InstanceEvent> events = game.instance().eventNode();

        // we need to update the player's hotbar whenever they spawn
        events.addListener(PlayerSpawnEvent.class, event -> {
            this.updateHotbar(event.getPlayer());
        });

        events.addListener(PlayerPacketEvent.class, event -> {
            Player player = event.getPlayer();

            if (!(event.getPacket() instanceof ClientHeldItemChangePacket changeHeldItemPacket)) {
                return;
            }

            player.getInstance().scheduleNextTick(ignored -> {
                this.updateHotbar(player);
            });

            ItemStack previous = player.getInventory().getItemStack(player.getHeldSlot());
            Color previousColor = Color.fromMaterial(previous.material());

            ItemStack current = player.getInventory().getItemStack(changeHeldItemPacket.slot());
            Color newColor = Color.fromMaterial(current.material());
            Util.log("Changing color from " + previousColor + " to " + newColor + " for " + player.getUsername() + ".");
        });
    }

    public void updateHotbar(Player player) {
        ItemStack heldItem = player.getInventory().getItemStack(player.getHeldSlot());
        @Nullable Color heldColor = Color.fromMaterial(heldItem.material());

        for (Color color : Color.values()) {
            game.blocks().setColor(color == heldColor, player, color);
        }
    }
}
