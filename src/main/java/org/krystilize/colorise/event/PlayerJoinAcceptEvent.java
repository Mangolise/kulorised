package org.krystilize.colorise.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

public record PlayerJoinAcceptEvent(Player player, Player other) implements Event {
}
