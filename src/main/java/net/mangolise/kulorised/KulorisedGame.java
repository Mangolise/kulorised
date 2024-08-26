package net.mangolise.kulorised;

import ch.qos.logback.classic.spi.LogbackServiceProvider;
import net.hollowcube.polar.PolarLoader;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.impl.JSONComponentSerializerProviderImpl;
import net.kyori.adventure.util.HSVLike;
import net.mangolise.gamesdk.BaseGame;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.mangolise.kulorised.blocks.SignBlock;
import net.mangolise.kulorised.commands.*;
import net.mangolise.kulorised.feats.HitSoundFeature;
import net.mangolise.kulorised.game.GameInfo;
import net.mangolise.kulorised.game.Level0Instance;
import net.mangolise.kulorised.leaderboard.LeaderboardManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.provider.MinestomComponentLoggerProvider;
import net.minestom.server.adventure.provider.MinestomGsonComponentSerializerProvider;
import net.minestom.server.adventure.provider.MinestomLegacyComponentSerializerProvider;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.scoreboard.TeamManager;
import net.minestom.server.sound.SoundEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class KulorisedGame extends BaseGame<KulorisedConfig> {
    public static final Pos SPAWN = new Pos(0.5, 37, 0.5, 90f, 0f);

    public KulorisedGame(KulorisedConfig config) {
        super(config);
    }

    @Override
    public List<Feature<?>> features() {
        return List.of(
            new HitSoundFeature()
        );
    }

    @Override
    public void setup() {
        super.setup();

        // Use some service providers here so they are not minimized
        System.out.println("Using logback: " + new LogbackServiceProvider());
        System.out.println("Using component serializer: " + new MinestomComponentLoggerProvider());
        System.out.println("Using legacy component serializer: " + new MinestomLegacyComponentSerializerProvider());
        System.out.println("Using gson component serializer: " + new MinestomGsonComponentSerializerProvider());
        System.out.println("Using json component serializer: " + new JSONComponentSerializerProviderImpl());

        // Register all sign blocks
        MinecraftServer.getBlockManager().registerHandler("minecraft:sign", () -> SignBlock.INSTANCE);

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
//        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
//            final Player player = event.getPlayer();
//            event.setSpawningInstance(lobbyInstance);
//
//            try {
//                player.sendResourcePacks(ResourcePackRequest.resourcePackRequest()
//                        .packs(ResourcePackInfo.resourcePackInfo()
//                                .id(UUID.randomUUID())
//                                .uri(new URI("https://github.com/KrystilizeNevaDies/ColoriseResourcePack/releases/download/latest/pack.zip"))
//                                .computeHashAndBuild().get())
//                        .required(true)
//                        .build());
//            } catch (URISyntaxException | ExecutionException | InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        });

//        NBSSong song;
//        try {
//            song = new NBSSong(Path.of("MiiChannelTheme.nbs"));
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }

        List<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers().stream().toList();
        if (players.size() != 2) {
            throw new IllegalStateException("Only 2 players allowed");
        }

        players.forEach(player -> {
            player.setRespawnPoint(SPAWN);
            player.setGameMode(GameMode.ADVENTURE);

            if (Util.ADMINS.contains(player.getUsername())) {
                player.setPermissionLevel(4);
            }

            player.setGlowing(true);
            player.setHelmet(ItemStack.of(Material.LEATHER_HELMET));

            // Play music
//            Util.loopSong(song, player);

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

        LeaderboardManager.setup();

        MinecraftServer.getCommandManager().register(new GameModeCommand());
        MinecraftServer.getCommandManager().register(new LeaderboardCommand());
        MinecraftServer.getCommandManager().register(new ToggleScoreboardCommand());
        MinecraftServer.getCommandManager().register(new LeaveCommand());

        // Start the game
        startGame(players.get(0), players.get(1));
    }

    private void startGame(Player p1, Player p2) {
        // Play sounds
        Sound startSound = Sound.sound(b -> b.type(SoundEvent.ENTITY_PLAYER_LEVELUP));
        p1.playSound(startSound);
        p2.playSound(startSound);

        // Make the game instance
        PolarLoader loader = GameSdkUtils.getPolarLoaderFromResource("worlds/level0.polar");
        InstanceContainer level0Instance = MinecraftServer.getInstanceManager().createInstanceContainer(loader);
        BlockAnalysis.analyse(level0Instance, loader.world());

        GameInfo info = new GameInfo(List.of(p1, p2), level0Instance);
        MinecraftServer.getInstanceManager().registerInstance(level0Instance);
        Level0Instance level0 = new Level0Instance(info);
        MinecraftServer.getInstanceManager().registerSharedInstance(level0);

        Component baseMsg = Component.text("Starting game with ").color(TextColor.fromHexString("#15eb6e"));
        p1.sendMessage(baseMsg.append(Component.text(p2.getUsername()).color(TextColor.fromHexString("#15eb6e")).decorate(TextDecoration.BOLD)));
        p2.sendMessage(baseMsg.append(Component.text(p1.getUsername()).color(TextColor.fromHexString("#15eb6e")).decorate(TextDecoration.BOLD)));

        // add players to the game, then start it
        Pos spawnPos = KulorisedGame.SPAWN.withYaw(270f);
        CompletableFuture.allOf(p1.setInstance(level0, spawnPos), p2.setInstance(level0, spawnPos))
                .thenRun(level0::start);
    }
}
