package org.krystilize.colorise;

import net.minestom.server.particle.Particle;
import net.minestom.server.world.biome.Biome;
import net.minestom.server.world.biome.BiomeEffects;
import net.minestom.server.world.biome.BiomeParticle;

import java.util.List;
import java.util.Map;

public class ColorBiomes {

    public static List<Map.Entry<String, Biome>> generateColorBiomes() {

        return List.of(
                Map.entry("bob", Biome.builder()
                        .effects(BiomeEffects.builder()
                                .grassColor(0xFF0000)
                                .foliageColor(0x0000FF)
                                .biomeParticle(new BiomeParticle(0.2f, Particle.FLAME))
                                .build())
                        .build()),
                Map.entry("john", Biome.builder()
                        .effects(BiomeEffects.builder()
                                .grassColor(0x00FF00)
                                .foliageColor(0xFF00FF)
                                .biomeParticle(new BiomeParticle(0.05f, Particle.SMOKE))
                                .build())
                        .build())
        );
    }
}
