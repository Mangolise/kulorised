package net.mangolise.kulorised.feats;

import net.kyori.adventure.sound.Sound;
import net.mangolise.gamesdk.Game;
import net.mangolise.kulorised.KulorisedGame;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.sound.SoundEvent;

// Hit sounds for fun
public class HitSoundFeature implements Game.Feature<KulorisedGame> {

    @Override
    public void setup(Context<KulorisedGame> context) {
        Sound sound = Sound.sound(b -> b.type(SoundEvent.ENTITY_PLAYER_ATTACK_NODAMAGE));
        MinecraftServer.getGlobalEventHandler().addListener(EntityAttackEvent.class, event -> {
            if (event.getEntity() instanceof Player p) {
                p.playSound(sound);
            }
        });
    }
}
