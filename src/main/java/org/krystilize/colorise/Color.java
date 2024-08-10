package org.krystilize.colorise;

import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Color {
    BLUE(0x0000FF, Block.BLUE_CONCRETE, Material.LIGHT_BLUE_CONCRETE, Block.BLUE_STAINED_GLASS),
    RED(0xFF0000, Block.RED_CONCRETE, Material.RED_CONCRETE, Block.RED_STAINED_GLASS),
    GREEN(0x00FF00, Block.GREEN_CONCRETE, Material.LIME_CONCRETE, Block.GREEN_STAINED_GLASS),
    YELLOW(0xFFFF00, Block.YELLOW_CONCRETE, Material.YELLOW_CONCRETE, Block.YELLOW_STAINED_GLASS)
    ;


    private final int color;
    private final Block block;
    private final Material material;
    private final Block glassBlock;

    Color(int color, Block block, Material material, Block glassBlock) {
        this.color = color;
        this.block = block;
        this.material = material;
        this.glassBlock = glassBlock;
    }

    public static Color fromBlockName(String blockName) {
        return switch (blockName) {
            case "minecraft:yellow_concrete" -> YELLOW;
            case "minecraft:lime_concrete" -> GREEN;
            case "minecraft:red_concrete" -> RED;
            case "minecraft:blue_concrete", "minecraft:light_blue_concrete" -> BLUE;
            default -> throw new IllegalArgumentException("Unknown block name: " + blockName);
        };
    }

    private static final Map<Material, Color> material2Color = Stream.of(Color.values())
            .collect(Collectors.toMap(Color::material, Function.identity()));
    public static @Nullable Color fromMaterial(Material material) {
        return material2Color.get(material);
    }

    public int color() {
        return color;
    }

    public Block block() {
        return block;
    }

    public Material material() {
        return material;
    }

    public Block glassBlock() { return glassBlock; }
}
