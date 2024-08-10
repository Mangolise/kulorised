package org.krystilize.colorise.game;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import org.krystilize.colorise.Color;

import java.util.List;
import java.util.Map;

public record GameInfo(List<Player> players, Map<Point, Color> blocks, InstanceContainer parent) {
}
