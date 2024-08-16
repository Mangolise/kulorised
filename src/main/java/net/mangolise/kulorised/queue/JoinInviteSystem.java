package net.mangolise.kulorised.queue;

import org.jetbrains.annotations.ApiStatus;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

public class JoinInviteSystem {

    public static final long INVITE_TIMEOUT_SECONDS = 60;

    private record Invite(UUID from, UUID to, long sentTime) {
        public boolean isExpired() {
            return System.currentTimeMillis() - sentTime > (INVITE_TIMEOUT_SECONDS * 1000);
        }
    }

    private static final Set<Invite> invites = ConcurrentHashMap.newKeySet();

    public static void invite(UUID from, UUID to) {
        invites.add(new Invite(from, to, System.currentTimeMillis()));
    }

    public static void remove(UUID from, UUID to) {
        invites.removeIf(invite -> invite.from.equals(from) && invite.to.equals(to));
    }

    public static boolean doesInviteExist(UUID from, UUID to) {
        return invites.stream().anyMatch(invite -> invite.from.equals(from) && invite.to.equals(to));
    }

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


    /**
     * Only call this method once on startup.
     */
    @ApiStatus.Internal
    public static void start() {
        scheduler.scheduleAtFixedRate(() -> {
            invites.removeIf(Invite::isExpired);
        }, 0, 1, TimeUnit.SECONDS);
    }
}
