package club.revived.proxy.service.player;

import club.revived.proxy.ProxyPlugin;
import club.revived.proxy.service.cluster.Cluster;
import club.revived.proxy.service.exception.UnregisteredPlayerException;
import club.revived.proxy.service.messaging.impl.SendMessage;
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

    /**
     * Initializes the PlayerManager singleton and registers message handlers for player-related messaging.
     */
    public PlayerManager() {
        System.out.println( "Starting player manager...");
        instance = this;

        this.registerMessageHandlers();
        System.out.println( "Started player manager...");
    }

    /**
     * Registers a new NetworkPlayer for the given UUID, username, and current server.
     *
     * @param uuid the player's UUID
     * @param username the player's username
     * @param currentServer the name of the server the player is currently connected to
     */
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

    /**
     * Registers the given NetworkPlayer in the manager so it can be looked up by UUID.
     *
     * @param networkPlayer the NetworkPlayer to register; its UUID is used as the map key
     */
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

    /**
     * Registers a handler that forwards incoming `SendMessage` messages to the corresponding Bukkit player.
     *
     * When a `SendMessage` is received the handler looks up the player by UUID and delivers the rich message.
     *
     * @throws UnregisteredPlayerException if a `SendMessage` targets a UUID with no registered player on this proxy
     */
    private void registerMessageHandlers() {
        System.out.println( "Registering player message handlers...");
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

        System.out.println( "Registered player message handlers...");
    }

    /**
         * Retrieve the NetworkPlayer for a given Bukkit player's UUID.
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
     * Retrieve the live mapping of registered players keyed by their UUID.
     *
     * @return the live map from UUID to NetworkPlayer; modifications to the returned map update this manager's registry
     */
    public Map<UUID, NetworkPlayer> getNetworkPlayers() {
        return networkPlayers;
    }

    /**
     * Accesses the singleton PlayerManager instance.
     *
     * @return the initialized PlayerManager singleton
     * @throws RuntimeException if the singleton has not been initialized
     */
    public static PlayerManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("Tried to access not existing instance");
        }

        return instance;
    }
}