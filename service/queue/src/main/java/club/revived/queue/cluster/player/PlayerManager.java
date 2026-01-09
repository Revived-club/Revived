package club.revived.queue.cluster.player;

import club.revived.queue.cluster.exception.UnregisteredPlayerException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class PlayerManager {

    private final Map<UUID, NetworkPlayer> networkPlayers = new ConcurrentHashMap<>();

    private static PlayerManager instance;

    /**
     * Creates a PlayerManager, assigns this object as the singleton instance, and registers message handlers for inter-server messaging.
     */
    public PlayerManager() {
        instance = this;

        this.registerMessageHandlers();
    }

    /**
     * Create and register a NetworkPlayer for the given identity.
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
     * Adds the given NetworkPlayer to the manager's registry keyed by the player's UUID.
     *
     * @param networkPlayer the NetworkPlayer to register; its UUID is used as the registry key
     */
    public void registerPlayer(final NetworkPlayer networkPlayer) {
        this.networkPlayers.put(
                networkPlayer.getUuid(),
                networkPlayer
        );
    }

    /**
     * Registers message handlers that deliver inter-server chat payloads to Bukkit players.
     *
     * <p>Installs two handlers on the cluster messaging service:
     * <ul>
     *   <li>SendMessage — resolves the target by UUID and sends the message to that player as a rich chat message.</li>
     *   <li>BroadcastMessage — sends the message as a rich chat message to every online player.</li>
     * </ul>
     *
     * @throws UnregisteredPlayerException if a SendMessage target UUID does not correspond to an online Bukkit player
     */
    private void registerMessageHandlers() {

    }

    /**
     * Resolve the NetworkPlayer mapped to the given Bukkit player UUID.
     *
     * @param uuid the Bukkit player's UUID to look up
     * @return the registered NetworkPlayer for the UUID
     * @throws UnregisteredPlayerException if no NetworkPlayer is registered for the UUID
     */
    @NotNull
    public NetworkPlayer fromBukkitPlayer(final UUID uuid) {
        if (!this.networkPlayers.containsKey(uuid)) {
            throw new UnregisteredPlayerException("tried to find a non existing player");
        }

        return this.networkPlayers.get(uuid);
    }

    /**
     * Finds a registered NetworkPlayer whose username matches the given name using a case-insensitive comparison.
     *
     * @param name the username to search for
     * @return the first matching NetworkPlayer if present, `null` otherwise
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
     * Get the live mapping of registered players keyed by their UUID.
     *
     * @return the live map from UUID to NetworkPlayer; modifying the returned map updates this manager's registry
     */
    public Map<UUID, NetworkPlayer> getNetworkPlayers() {
        return networkPlayers;
    }

    /**
     * Accesses the singleton PlayerManager instance.
     *
     * @return the initialized PlayerManager instance
     * @throws RuntimeException if the singleton has not been initialized
     */
    public static PlayerManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("Tried to access not existing instance");
        }

        return instance;
    }
}