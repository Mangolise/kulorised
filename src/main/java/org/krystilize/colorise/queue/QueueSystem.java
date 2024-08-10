package org.krystilize.colorise.queue;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import org.krystilize.colorise.Util;
import org.krystilize.colorise.game.GameInfo;
import org.krystilize.colorise.game.Level0Instance;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public record QueueSystem(Instance lobby) {
    public QueueSystem {
        lobby.setTag(LEVEL0_INSTANCE, MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader("worlds/level0")));
        lobby.eventNode().addListener(InstanceTickEvent.class, event -> updateQueue());
    }

    private static final Tag<Queue<Player>> QUEUED_PLAYERS = Tag.<Queue<Player>>Transient("queued_players").defaultValue(new ArrayDeque<>());
    private static final Tag<InstanceContainer> LEVEL0_INSTANCE = Tag.Transient("level0_instance");

    public void addPlayer(Player player) {
        Util.log(player.getUsername() + " joined the queue");

        // Add the player to the queue
        lobby.updateTag(QUEUED_PLAYERS, players -> {
            if (players.contains(player)) return players;
            players.add(player);
            return players;
        });
    }

    public void removePlayer(Player player) {
        Util.log(player.getUsername() + " left the queue");

        // Add the player to the queue
        lobby.updateTag(QUEUED_PLAYERS, players -> {
            players.remove(player);
            return players;
        });
    }

    public void updateQueue() {
        Queue<Player> players = lobby.getTag(QUEUED_PLAYERS);
        synchronized (this) {
            if (players.size() < 2) {
                // not enough players
                return;
            }

            Player p1 = players.remove();
            Player p2 = players.remove();

            // Play sounds
            Sound startSound = Sound.sound(b -> b.type(SoundEvent.ENTITY_PLAYER_LEVELUP));
            p1.playSound(startSound);
            p2.playSound(startSound);

            Util.log("Starting game with " + p1.getUsername() + " and " + p2.getUsername());

            InstanceContainer level0Instance = lobby.getTag(LEVEL0_INSTANCE);

            GameInfo info = new GameInfo(List.of(p1, p2), level0Instance);
            MinecraftServer.getInstanceManager().registerInstance(level0Instance);
            Level0Instance level0 = new Level0Instance(this, info);
            MinecraftServer.getInstanceManager().registerSharedInstance(level0);

            // add players to the game
            p1.setInstance(level0);
            p2.setInstance(level0);

            // start the game
            level0.start();
        }
    }
}
