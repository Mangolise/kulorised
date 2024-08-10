package org.krystilize.colorise.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.block.Block;
import org.krystilize.colorise.Color;

public class BlockOutlineDisplayEntity extends Entity {
    public BlockOutlineDisplayEntity(Color color) {
        super(EntityType.BLOCK_DISPLAY);

        setNoGravity(true);

        editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setBlockState(Block.GLASS);
            meta.setBrightnessOverride(15 << 4 | 15 << 20); // blockLight << 4 | skyLight << 20
            meta.setGlowColorOverride(color.color());
            meta.setInvisible(true);
        });

        setGlowing(true);
    }
}
