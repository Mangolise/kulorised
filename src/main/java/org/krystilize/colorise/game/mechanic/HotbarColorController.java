package org.krystilize.colorise.game.mechanic;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientHeldItemChangePacket;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.krystilize.colorise.Color;
import org.krystilize.colorise.Util;
import org.krystilize.colorise.event.ColorChangeEvent;
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
            event.getInstance().scheduler().scheduleTask(() -> this.updateHotbar(player), TaskSchedule.seconds(1), TaskSchedule.stop());
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

            instance.scheduleNextTick(ignored -> this.updateHotbar(player));
        });
    }

    public void updateHotbar(Player player) {
        ItemStack heldItem = player.getInventory().getItemStack(player.getHeldSlot());
        @Nullable Color heldColor = Color.fromMaterial(heldItem.material());

        ColorChangeEvent event = new ColorChangeEvent(heldColor, player);
        EventDispatcher.call(event);

        player.setTeam(Util.COLOR_TEAMS.get(heldColor));
    }
}
