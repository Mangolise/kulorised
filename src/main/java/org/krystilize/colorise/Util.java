package org.krystilize.colorise;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class Util {

    public static void forEachNonAirBlockInChunk(Chunk chunk, BiConsumer<Point, Block> consumer) {
        // for each block in the chunk, check if it is air
        for (int blockX = chunk.getChunkX() * 16; blockX < chunk.getChunkX() * 16 + 16; blockX++) {
            for (int blockY = chunk.getMinSection() * 16; blockY < chunk.getMaxSection() * 16; blockY++) {
                for (int blockZ = chunk.getChunkZ() * 16; blockZ < chunk.getChunkZ() * 16 + 16; blockZ++) {
                    if (chunk.getBlock(blockX, blockY, blockZ).compare(Block.AIR)) {
                        continue;
                    }
                    consumer.accept(new Vec(blockX, blockY, blockZ), chunk.getBlock(blockX, blockY, blockZ));
                }
            }
        }
    }

    private static final IntSet coloredBlocks = IntSet.of(
            Block.values().stream()
                    .filter(block -> block.name().endsWith("wool"))
                    .filter(block -> !block.name().contains("white"))
                    .filter(block -> !block.name().contains("gray"))
                    .filter(block -> !block.name().contains("black"))
                    .mapToInt(Block::stateId)
                    .toArray()
    );
    public static boolean isColoredBlock(Block block) {
        return coloredBlocks.contains(block.stateId());
    }

    public static boolean isWindowPane(Block block) {
        return block.name().endsWith("_stained_glass_pane") &&
                !block.name().contains("black");
    }

    public static boolean isPressurePlate(Block block) {
        return block.equals(Block.LIGHT_WEIGHTED_PRESSURE_PLATE) || block.equals(Block.HEAVY_WEIGHTED_PRESSURE_PLATE);
    }

    public static void setPlayerGamemode(Player player, GameMode gamemode) {
        switch (gamemode) {
            case CREATIVE -> {
                player.setGameMode(GameMode.CREATIVE);
                player.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE).setBaseValue(4.5);
            }

            case ADVENTURE -> {
                player.setGameMode(GameMode.ADVENTURE);
                player.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE).setBaseValue(-1);
            }

            case SPECTATOR -> player.setGameMode(GameMode.SPECTATOR);
        }
    }

    public static final Set<String> ADMINS = Set.of("Calcilore", "EclipsedMango", "Krystilize", "CoPokBl");
    public static final Map<Color, Team> COLOR_TEAMS = new HashMap<>();

    public static void log(Object log) {
        String logString = log.toString();
        Audiences.all().sendMessage(Component.text(logString));
    }

    public static boolean lacksPermission(CommandSender sender) {
        if (sender instanceof ConsoleSender) {
            return false;
        }

        if (sender instanceof Player p) {
            if (!Util.ADMINS.contains(p.getUsername())) {  // bad
                p.sendMessage("You do not have permission.");
                return true;
            }
            return false;
        }

        return true;
    }

    public static boolean isPlayer(CommandSender commandSender, @Nullable String s) {
        return commandSender instanceof Player;
    }
}
