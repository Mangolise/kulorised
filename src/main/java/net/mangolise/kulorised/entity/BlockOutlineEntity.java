package net.mangolise.kulorised.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.mangolise.kulorised.Color;

public class BlockOutlineEntity extends Entity {
    public BlockOutlineEntity(Color color) {
        super(EntityType.BLOCK_DISPLAY);

        setNoGravity(true);

        editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setBlockState(color.glassBlock());
            meta.setBrightnessOverride(15 << 4 | 15 << 20); // blockLight << 4 | skyLight << 20
            meta.setGlowColorOverride(color.color());
        });

        setGlowing(true);
    }
}
