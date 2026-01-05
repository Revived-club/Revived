package club.revived.proxy.service.player;

import club.revived.proxy.ProxyPlugin;
import club.revived.proxy.service.cluster.Cluster;
import club.revived.proxy.service.exception.UnregisteredPlayerException;
import club.revived.proxy.service.player.impl.SendMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class PlayerManager {

    private static final Logger log = LoggerFactory.getLogger(PlayerManager.class);
    private final Map<UUID, NetworkPlayer> networkPlayers = new HashMap();

    private static PlayerManager instance;

    public PlayerManager() {
        instance = this;

        this.registerMessageHandlers();
    }

    public void registerPlayer(
            final @NotNull UUID uuid,
            final @NotNull String username,
            final @NotNull String currentServer
    ) {
        final var player = new NetworkPlayer(
                uuid,
                username,
                currentServer
        );

        this.registerPlayer(player);
    }

    public void registerPlayer(final NetworkPlayer networkPlayer) {
        log.info("Registered player {} - {}",
                networkPlayer.getUsername(),
                networkPlayer.getUuid()
        );

        this.networkPlayers.put(
                networkPlayer.getUuid(),
                networkPlayer
        );
    }

    private void registerMessageHandlers() {
        Cluster.getInstance().getMessagingService()
                .registerMessageHandler(SendMessage.class, message -> {
                    final var uuid = message.uuid();
                    ProxyPlugin.getInstance()
                            .getServer()
                            .getPlayer(uuid)
                            .ifPresentOrElse(player -> {
                                player.sendRichMessage(message.message());
                            }, () -> {
                                throw new UnregisteredPlayerException("tried to message unregistered player");
                            });
                });
    }

    /**
     * Retrieve the NetworkPlayer associated with the given Bukkit player's UUID.
     *
     * @param uuid the Bukkit player's UUID used to look up the NetworkPlayer
     * @return the NetworkPlayer mapped to the UUID, or {@code null} if no mapping exists
     * @throws UnregisteredPlayerException if the internal registry contains the given UUID
     */
    @NotNull
    public NetworkPlayer fromVelocityPlayer(final UUID uuid) {
        if (this.networkPlayers.containsKey(uuid)) {
            throw new UnregisteredPlayerException("tried to find a non existing player");
        }

        return this.networkPlayers.get(uuid);
    }

    /**
     * Finds a registered NetworkPlayer by username using a case-insensitive match.
     *
     * @param name the username to search for (case-insensitive)
     * @return the first matching NetworkPlayer, or `null` if no player has the given username
     */
    @Nullable
    public NetworkPlayer withName(final String name) {
        final var players = this.networkPlayers.values()
                .stream()
                .filter(networkPlayer -> networkPlayer.getUsername().equalsIgnoreCase(name))
                .toList();

        if (players.isEmpty()) {
            return null;
        }

        return players.getFirst();
    }

    /**
     * Gets the internal mapping of registered players keyed by their UUID.
     *
     * @return the live map of UUID to NetworkPlayer for all registered players; modifications to the returned map affect this manager
     */
    public Map<UUID, NetworkPlayer> getNetworkPlayers() {
        return networkPlayers;
    }

    public static PlayerManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("Tried to access not existing instance");
        }

        return instance;
    }
}