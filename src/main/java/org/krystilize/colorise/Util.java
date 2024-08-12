package org.krystilize.colorise;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
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

    public static boolean isStainedGlass(Block block) {
        return block.name().endsWith("_stained_glass");
    }

    public static boolean isTerracotta(Block block) {
        return block.name().endsWith("_terracotta");
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

    public static List<Point> neighbors(Point pos) {
        return List.of(
                pos.add(1, 0, 0),
                pos.add(-1, 0, 0),
                pos.add(0, 1, 0),
                pos.add(0, -1, 0),
                pos.add(0, 0, 1),
                pos.add(0, 0, -1)
        );
    }

    public static void playerAction(Player player, String title, String subtitle, @Nullable Point pos) {
        Title.Times times = Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(300), Duration.ofMillis(100));
        player.showTitle(Title.title(Component.text(title), Component.text(subtitle, NamedTextColor.GREEN), times));
        Sound sound = Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_CHIME, Sound.Source.BLOCK, 0.7f, 1);
        if (pos != null) {
            player.getInstance().playSound(sound, pos);
        } else {
            player.playSound(sound);
        }
    }
}
