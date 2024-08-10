package org.krystilize.colorise.game;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;

import java.util.List;

public record GameInfo(List<Player> players, InstanceContainer parent) {
}
