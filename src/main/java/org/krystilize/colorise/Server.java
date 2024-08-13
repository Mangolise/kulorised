package org.krystilize.colorise;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.scoreboard.TeamManager;
import net.minestom.server.sound.SoundEvent;
import org.krystilize.colorise.blocks.SignBlock;
import org.krystilize.colorise.commands.GameModeCommand;
import org.krystilize.colorise.commands.ObserveCommand;
import org.krystilize.colorise.commands.JoinCommand;
import org.krystilize.colorise.commands.ShoutCommand;
import net.minestom.server.item.Material;
import org.krystilize.colorise.commands.*;
import org.krystilize.colorise.leaderboard.LeaderboardManager;
import org.krystilize.colorise.queue.JoinInviteSystem;
import org.krystilize.colorise.queue.QueueSystem;

import java.util.Objects;

public class Server {

    public static final Pos SPAWN = new Pos(0.5, 37, 0.5, 90f, 0f);

    public static void main(String[] args) {
        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();

        // Register all sign blocks
        MinecraftServer.getBlockManager().registerHandler("minecraft:sign", () -> SignBlock.INSTANCE);

        // Create the instance
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer lobbyInstance = instanceManager.createInstanceContainer(new AnvilLoader("worlds/lobby"));
        lobbyInstance.setChunkSupplier(LightingChunk::new);

        QueueSystem queueSystem = new QueueSystem(lobbyInstance);

//        Util.DEBUG_slowlySwapColors(coloredBlocks);

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(lobbyInstance);
            player.setRespawnPoint(SPAWN);
            player.setGameMode(GameMode.ADVENTURE);

            if (Util.ADMINS.contains(player.getUsername())) {
                player.setPermissionLevel(4);
            }
        });

        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            if (!event.isFirstSpawn()) return;

            Player player = event.getPlayer();
            //queueSystem.addPlayer(player);
            player.setGlowing(true);

            Util.sendNotification(player, "Welcome to our game :)", NamedTextColor.YELLOW, FrameType.TASK, Material.GOLDEN_HELMET);

            Util.broadcast(Component
                    .text("[").color(TextColor.fromHexString("#a1a1a1"))
                    .append(Component.text("+").color(TextColor.fromHexString("#2bd91e")))
                    .append(Component.text("] ").color(TextColor.fromHexString("#a1a1a1"))
                    .append(Component.text(player.getUsername() + " joined the game").color(TextColor.fromHexString("#2bd91e")))));
        });

        globalEventHandler.addListener(PlayerDisconnectEvent.class, event -> Util.broadcast(Component
                .text("[").color(TextColor.fromHexString("#a1a1a1"))
                .append(Component.text("-").color(TextColor.fromHexString("#e02a1d")))
                .append(Component.text("] ").color(TextColor.fromHexString("#a1a1a1"))
                .append(Component.text(event.getPlayer().getUsername() + " left the game").color(TextColor.fromHexString("#e02a1d"))))));

        {
            TeamManager tm = MinecraftServer.getTeamManager();

            for (Color color : Color.values()) {
                Team team = tm.createTeam(color.name());
                team.setTeamColor(NamedTextColor.nearestTo(TextColor.color(color.color())));
                Util.COLOR_TEAMS.put(color, team);
            }

            Team team = tm.createTeam("WHITE");
            Util.COLOR_TEAMS.put(null, team);
        }

        globalEventHandler.addListener(PlayerDisconnectEvent.class, event -> queueSystem.removePlayer(event.getPlayer()));

        globalEventHandler.addListener(ItemDropEvent.class, event -> event.setCancelled(true));

        Sound sound = Sound.sound(b -> b.type(SoundEvent.ENTITY_PLAYER_ATTACK_NODAMAGE));
        globalEventHandler.addListener(EntityAttackEvent.class, event -> {
            if (event.getEntity() instanceof Player p) {
                p.playSound(sound);
            }
        });

        globalEventHandler.addListener(ServerListPingEvent.class, event -> {
            ResponseData data = event.getResponseData();

            // Title colour cycles based on system time so it is random
            float randomHue = (System.currentTimeMillis() % 10000) / 10000f;
            TextColor titleColor = TextColor.color(HSVLike.hsvLike(randomHue, 0.8f, 0.8f));

            data.setDescription(Component
                    .text("Kulorised")
                    .color(titleColor)
                    .append(Component.text(" - A game of colors")
                    .color(TextColor.fromHexString("#a1a1a1")))
                    .append(Component
                    .text("\nby CoPokBl, Calcilore, EclipsedMango, Krystilize")
                    .color(TextColor.fromHexString("#a1a1a1"))));
            data.setMaxPlayer(6969);
            event.setResponseData(data);
        });

        //noinspection UnstableApiUsage
        lobbyInstance.eventNode().addListener(PlayerMoveEvent.class, event -> {
            if (event.getPlayer().getPosition().y() < 0) {
                event.getPlayer().teleport(SPAWN);
            }
        });

        JoinInviteSystem.start();

        boolean enableSkins = Objects.equals(System.getenv("ENABLE_SKINS"), "true");
        if (enableSkins) {
            new SkinHandler(globalEventHandler);  // Start skin handler
            System.out.println("Skins enabled.");
        } else {
            System.out.println("Skins disabled.");
        }

        LeaderboardManager.setup();

        MinecraftServer.getCommandManager().register(new GameModeCommand());
        MinecraftServer.getCommandManager().register(new ObserveCommand(queueSystem, lobbyInstance));
        MinecraftServer.getCommandManager().register(new JoinCommand());
        MinecraftServer.getCommandManager().register(new ShoutCommand());
        MinecraftServer.getCommandManager().register(new LeaderboardCommand());

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);
    }
}
