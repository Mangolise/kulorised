package net.mangolise.kulorised.blocks;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public enum SignBlock implements BlockHandler {
    INSTANCE;

    @Override
    public @NotNull Key getKey() {
        return Key.key("minecraft:sign");
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return Set.of(Tag.Byte("is_waxed"), Tag.NBT("front_text"), Tag.NBT("back_text"));
    }
}
