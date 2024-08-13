package org.krystilize.colorise;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.scoreboard.TeamManager;
import net.minestom.server.sound.SoundEvent;
import org.krystilize.colorise.blocks.SignBlock;
import org.krystilize.colorise.commands.GameModeCommand;
import org.krystilize.colorise.commands.ObserveCommand;
import org.krystilize.colorise.commands.JoinCommand;
import org.krystilize.colorise.commands.ShoutCommand;
import org.krystilize.colorise.queue.JoinInviteSystem;
import org.krystilize.colorise.queue.QueueSystem;

import java.util.Objects;

public class Server {

    public static final Pos SPAWN = new Pos(0.5, 37, 0.5, -90f, 0f);

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
            queueSystem.addPlayer(player);
            player.setGlowing(true);
        });

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

        JoinInviteSystem.start();

        boolean enableSkins = Objects.equals(System.getenv("ENABLE_SKINS"), "true");
        if (enableSkins) {
            new SkinHandler(globalEventHandler);  // Start skin handler
            System.out.println("Skins enabled.");
        } else {
            System.out.println("Skins disabled.");
        }

        MinecraftServer.getCommandManager().register(new GameModeCommand());
        MinecraftServer.getCommandManager().register(new ObserveCommand(queueSystem));
        MinecraftServer.getCommandManager().register(new JoinCommand());
        MinecraftServer.getCommandManager().register(new ShoutCommand());

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);
    }
}
