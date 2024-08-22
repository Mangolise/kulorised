package net.mangolise.kulorised.event;

import net.minestom.server.event.Event;

public record GameEndEvent(boolean didWin) implements Event { }
