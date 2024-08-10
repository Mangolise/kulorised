package org.krystilize.colorise.game.mechanic;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientHeldItemChangePacket;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.krystilize.colorise.Color;
import org.krystilize.colorise.Util;
import org.krystilize.colorise.game.ColoriseGame;
import org.krystilize.colorise.game.GameInstance;

/**
 * Controls setting colors when a player changes their hotbar selection
 */
public class HotbarColorController implements Mechanic {

    private @UnknownNullability ColoredBlockManagerMechanic coloredBlockManager;

    @Override
    public void setup(Context context) {
        ColoriseGame game = context.game();
        GameInstance instance = context.instance();
        coloredBlockManager = context.mechanic(ColoredBlockManagerMechanic.class);


        context.events().addListener(PlayerSpawnEvent.class, event -> {
            Player player = event.getPlayer();
            for (Color color : Color.values()) {
                coloredBlockManager.setColor(false, player, color);
            }
        });

        // we need to update the player's hotbar whenever they spawn
        context.events().addListener(PlayerPacketEvent.class, event -> {
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
            coloredBlockManager.setColor(color == heldColor, target, color);
        }
    }
}
