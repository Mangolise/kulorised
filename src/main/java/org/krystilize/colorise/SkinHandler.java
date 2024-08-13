package org.krystilize.colorise;

import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerSkinInitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SkinHandler {
    private final HashMap<String, PlayerSkin> skinCache = new HashMap<>();
    private final List<Long> apiRequests = new ArrayList<>();

    public SkinHandler(GlobalEventHandler events) {
        events.addListener(PlayerSkinInitEvent.class, e -> {
            e.setSkin(getSkin(e.getPlayer().getUsername()));
            System.out.println("Setting skin for " + e.getPlayer().getUsername());
        });
    }

    private PlayerSkin getSkin(String username) {
        if (skinCache.containsKey(username)) {
            return skinCache.get(username);
        }

        if (isRateLimited()) {
            // uhhhh shit
            System.out.println("Slowing down... We might get ratelimited");
            return null;
        }

        PlayerSkin skin = PlayerSkin.fromUsername(username);
        skinCache.put(username, skin);
        return skin;
    }

    // 600 requests per 10 minutes
    private void pruneRequests() {
        long now = System.currentTimeMillis();
        apiRequests.removeIf(time -> now - time > 600_000);
    }

    private boolean isRateLimited() {
        pruneRequests();
        return apiRequests.size() >= 600;
    }
}
