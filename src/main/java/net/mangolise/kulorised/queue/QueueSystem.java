//package net.mangolise.kulorised.queue;
//
//import net.kyori.adventure.sound.Sound;
//import net.kyori.adventure.text.Component;
//import net.kyori.adventure.text.format.TextColor;
//import net.kyori.adventure.text.format.TextDecoration;
//import net.mangolise.kulorised.KulorisedGame;
//import net.minestom.server.MinecraftServer;
//import net.minestom.server.coordinate.Pos;
//import net.minestom.server.entity.Player;
//import net.minestom.server.event.instance.InstanceTickEvent;
//import net.minestom.server.event.player.PlayerBlockInteractEvent;
//import net.minestom.server.event.player.PlayerChatEvent;
//import net.minestom.server.instance.Instance;
//import net.minestom.server.instance.InstanceContainer;
//import net.minestom.server.instance.anvil.AnvilLoader;
//import net.minestom.server.instance.block.Block;
//import net.minestom.server.sound.SoundEvent;
//import net.minestom.server.tag.Tag;
//import net.mangolise.kulorised.BlockAnalysis;
//import net.mangolise.kulorised.Test;
//import net.mangolise.kulorised.Util;
//import net.mangolise.kulorised.event.PlayerJoinAcceptEvent;
//import net.mangolise.kulorised.game.GameInfo;
//import net.mangolise.kulorised.game.GameInstance;
//import net.mangolise.kulorised.game.Level0Instance;
//
//import java.util.*;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ConcurrentHashMap;
//
//public record QueueSystem(Instance lobby) {
//
//    @SuppressWarnings("UnstableApiUsage")
//    public QueueSystem {
//        InstanceContainer level0Instance = MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader("worlds/level0"));
//        BlockAnalysis.analyse(level0Instance);
//
//        lobby.setTag(LEVEL0_INSTANCE, level0Instance);
//        lobby.eventNode().addListener(InstanceTickEvent.class, event -> updateQueue());
//
//        MinecraftServer.getGlobalEventHandler().addListener(PlayerJoinAcceptEvent.class, this::handleJoinAccept);
//        MinecraftServer.getGlobalEventHandler().addListener(PlayerChatEvent.class, this::handleChat);
//
//        lobby.eventNode().addListener(PlayerBlockInteractEvent.class, event -> {
//            Player p = event.getPlayer();
//
//            if (!Block.OAK_BUTTON.compare(event.getBlock())) {
//                return;
//            }
//
//            if (isQueued(p)) {
//                return;
//            }
//
//            addPlayer(p);
//        });
//    }
//
//    private record Match(UUID player1, UUID player2) {
//        public boolean isMatched(Player player) {
//            return player.getUuid().equals(player1) || player.getUuid().equals(player2);
//        }
//    }
//
//    private static final Tag<Queue<Player>> QUEUED_PLAYERS = Tag.<Queue<Player>>Transient("queued_players").defaultValue(new ArrayDeque<>());
//    private static final Tag<List<Player>> EXEMPT_PLAYERS = Tag.<List<Player>>Transient("exempt_players").defaultValue(new ArrayList<>());
//    private static final Tag<InstanceContainer> LEVEL0_INSTANCE = Tag.Transient("level0_instance");
//
//    private static final Tag<Set<Match>> FORCED_MATCHES = Tag.<Set<Match>>Transient("forced_matches").defaultValue(ConcurrentHashMap.newKeySet());
//
//    public boolean isQueued(Player player) {
//        return lobby.getTag(QUEUED_PLAYERS).contains(player);
//    }
//
//    public void setPlayerExempt(Player player, boolean exempt) {
//        lobby.updateTag(EXEMPT_PLAYERS, players -> {
//            players.remove(player);
//            if (exempt) {
//                players.add(player);
//            }
//            return players;
//        });
//    }
//
//    public boolean isExempt(Player player) {
//        return lobby.getTag(EXEMPT_PLAYERS).contains(player);
//    }
//
//    public void addPlayer(Player player) {
//        if (isExempt(player)) return;
//
//        player.sendMessage(Component.text("You have been added to the queue").color(TextColor.fromHexString("#15eb6e")));
//
//        // Add the player to the queue
//        lobby.updateTag(QUEUED_PLAYERS, players -> {
//            if (players.contains(player)) return players;
//            players.add(player);
//            return players;
//        });
//    }
//
//    public void resetPlayer(Player player) {
//        // Reset their pos and clear inventory
//        if (player.getInstance() != lobby) player.setInstance(lobby);
//        player.setRespawnPoint(KulorisedGame.SPAWN);
//        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
//            if (player.getInstance() == null) return;
//            player.teleport(player.getRespawnPoint());
//        });
//        player.getInventory().clear();
//    }
//
//    public void removePlayer(Player player) {
//        // Remove the player from the queue
//        lobby.updateTag(QUEUED_PLAYERS, players -> {
//            players.remove(player);
//            return players;
//        });
//    }
//
//    public synchronized void updateQueue() {
//        Queue<Player> queudPlayers = lobby.getTag(QUEUED_PLAYERS);
//
//        // check if there are any forced matches
//        Set<Match> forcedMatches = lobby.getTag(FORCED_MATCHES);
//        {
//            for (var iterator = forcedMatches.iterator(); iterator.hasNext(); ) {
//                Match match = iterator.next();
//
////            Player p1 = queudPlayers.stream()
////                    .filter(player -> player.getUuid().equals(match.player1()))
////                    .findAny()
////                    .orElse(null);
////            Player p2 = queudPlayers.stream()
////                    .filter(player -> player.getUuid().equals(match.player2()))
////                    .findAny()
////                    .orElse(null);
//                Player p1 = lobby.getPlayerByUuid(match.player1);
//                Player p2 = lobby.getPlayerByUuid(match.player2);
//
//                if (p1 != null && p2 != null) {
//                    // match exists, start a game
//                    queudPlayers.remove(p1);
//                    queudPlayers.remove(p2);
//
//                    startGame(p1, p2);
//
//                    iterator.remove();
//                    lobby.setTag(FORCED_MATCHES, forcedMatches);
//
//                    queuePositionUpdate(queudPlayers);
//                    return;
//                }
//            }
//        }
//
//        if (queudPlayers.size() >= 2) {
//
//            // start a game with the first two players that are not part of a forced match
//            Player p1 = queudPlayers.stream().filter(player -> forcedMatches.stream().noneMatch(match -> match.isMatched(player))).findFirst().orElse(null);
//            Player p2 = queudPlayers.stream().filter(player -> forcedMatches.stream().noneMatch(match -> match.isMatched(player))).skip(1).findFirst().orElse(null);
//
//            if (p1 != null && p2 != null) {
//                queudPlayers.remove(p1);
//                queudPlayers.remove(p2);
//
//                startGame(p1, p2);
//            }
//        }
//
//        // tell each player in the queue their position
//        queuePositionUpdate(queudPlayers);
//    }
//
//    private void queuePositionUpdate(Queue<Player> players) {
//        int i = 1;
//        for (Player player : players) {
//            player.sendActionBar(Component.text("Position in queue: " + i + " / " + players.size()));
//            i++;
//        }
//    }
//
//    private void handleJoinAccept(PlayerJoinAcceptEvent event) {
//        // remove both players from their game
//        Player player = event.player();
//        Player other = event.other();
//
//        GameInstance instance1 = player.getInstance() instanceof GameInstance game ? game : null;
//        GameInstance instance2 = other.getInstance() instanceof GameInstance game ? game : null;
//
//        // register a forced match
//        lobby.updateTag(FORCED_MATCHES, matches -> {
//            matches.add(new Match(player.getUuid(), other.getUuid()));
//            return matches;
//        });
//
//        // stop both the games
//        if (instance1 != null) {
//            instance1.stop();
//            if (instance1 != instance2 && instance2 != null) {
//                instance2.stop();
//            }
//        }
//
//        // remove the join accept event
//        JoinInviteSystem.remove(player.getUuid(), other.getUuid());
//    }
//
//    private void handleChat(PlayerChatEvent e) {
//        if (e.getPlayer().getInstance() instanceof GameInstance) {
//            return;  // Will be handled by the game chat
//        }
//
//        e.setCancelled(true);
//
//        Component message = Util.formatMessage("LOBBY", e.getPlayer(), e.getMessage());
//        for (Player p : lobby.getPlayers()) {
//            p.sendMessage(message);
//        }
//    }
//}
