package net.mangolise.kulorised.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import net.mangolise.kulorised.Color;

public class ColorChangeEvent implements PlayerEvent, InstanceEvent {

    private final Color color;
    private final Player player;

    public ColorChangeEvent(Color color, Player player) {
        this.color = color;
        this.player = player;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public @NotNull Instance getInstance() {
        return player.getInstance();
    }
}
