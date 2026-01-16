package club.revived.lobby.game.player;

import club.revived.lobby.database.DatabaseManager;
import club.revived.lobby.service.cluster.Cluster;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * PlayerProfileManager
 *
 * @author yyuh - DL
 * @since 1/16/26
 */
public final class PlayerProfileManager {

    private static PlayerProfileManager instance;

    public PlayerProfileManager() {
        instance = this;
        this.setup();
    }

    public void setup() {
        DatabaseManager.getInstance()
                .getAll(PlayerProfile.class)
                .thenAccept(playerProfiles -> {
                    for (final var profile : playerProfiles) {
                        this.get(profile.uuid()).thenAccept(redisProfile -> {
                            if (redisProfile != null && redisProfile.equals(profile)) {
                                return;
                            }

                            Cluster.getInstance()
                                    .getGlobalCache()
                                    .set("profile:" + profile.uuid(), profile);
                        });
                    }
                });
    }

    @NotNull
    public CompletableFuture<List<PlayerProfile>> getAll(final List<UUID> uuids) {
        final List<CompletableFuture<PlayerProfile>> futures = uuids.stream()
                .map(this::getOrLoad)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(_ -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList()
                );
    }

    public void update(
            final UUID uuid,
            final String username,
            final String skin,
            final long lastJoin
    ) {
        Cluster.getInstance()
                .getGlobalCache()
                .get(PlayerProfile.class, "profile:" + uuid)
                .thenAccept(_ -> {
                    final var profile = new PlayerProfile(
                            uuid,
                            username,
                            skin,
                            lastJoin
                    );

                    Cluster.getInstance()
                            .getGlobalCache()
                            .set("profile:" + uuid, profile);

                    DatabaseManager.getInstance().save(PlayerProfile.class, profile);
                });
    }

    @NotNull
    public CompletableFuture<PlayerProfile> getOrLoad(final UUID uuid) {
        return Cluster.getInstance()
                .getGlobalCache()
                .get(PlayerProfile.class, "profile:" + uuid)
                .thenCompose(t -> {
                    if (t != null) {
                        return CompletableFuture.completedFuture(t);
                    }

                    return DatabaseManager.getInstance().get(PlayerProfile.class, uuid.toString())
                            .thenApply(opt -> {
                                final var val = opt.orElse(null);
                                if (val != null) {
                                    Cluster.getInstance().getGlobalCache().set("profile:" + uuid, val);
                                }
                                return val;
                            });
                });
    }

    @NotNull
    public CompletableFuture<PlayerProfile> get(final UUID uuid) {
        return Cluster.getInstance()
                .getGlobalCache()
                .get(PlayerProfile.class, "profile:" + uuid);
    }

    public static PlayerProfileManager getInstance() {
        if (instance == null) {
            return new PlayerProfileManager();
        }

        return instance;
    }
}
