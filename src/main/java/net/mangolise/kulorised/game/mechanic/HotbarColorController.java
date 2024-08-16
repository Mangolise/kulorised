package net.mangolise.kulorised.game.mechanic;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientHeldItemChangePacket;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import net.mangolise.kulorised.Color;
import net.mangolise.kulorised.Util;
import net.mangolise.kulorised.event.ColorChangeEvent;
import net.mangolise.kulorised.game.ColoriseGame;
import net.mangolise.kulorised.game.GameInstance;

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

        instance.scheduler()
                .scheduleTask(() -> {
                    game.players().forEach(this::updateHotbar);
                    return TaskSchedule.stop();
                }, TaskSchedule.seconds(1));

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

//            ItemStack previous = player.getInventory().getItemStack(player.getHeldSlot());
//            Color previousColor = Color.fromMaterial(previous.material());
//            ItemStack current = player.getInventory().getItemStack(changeHeldItemPacket.slot());
//            Color newColor = Color.fromMaterial(current.material());
//            Util.log("Changing color from " + previousColor + " to " + newColor + " for " + player.getUsername() + ".");

            ItemStack newItem = player.getInventory().getItemStack(changeHeldItemPacket.slot());
            if (newItem.isAir()) {
                event.setCancelled(true);
                // go to the opposite of the player's current slot
                if (player.getHeldSlot() == 0) {
                    player.setHeldItemSlot((byte) 1);
                } else {
                    player.setHeldItemSlot((byte) 0);
                }
            }

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
