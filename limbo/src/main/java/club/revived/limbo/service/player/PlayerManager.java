package club.revived.limbo.service.player;

import club.revived.commons.inventories.util.ColorUtils;
import club.revived.limbo.service.cluster.Cluster;
import club.revived.limbo.service.exception.UnregisteredPlayerException;
import club.revived.limbo.service.messaging.impl.SendActionbar;
import club.revived.limbo.service.messaging.impl.SendMessage;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.player.Player;
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
    private final Map<UUID, NetworkPlayer> networkPlayers = new HashMap<>();

    private static PlayerManager instance;

    /**
     * Creates a PlayerManager, assigns this object as the singleton instance, and registers message handlers for inter-server messaging.
     */
    public PlayerManager() {
        instance = this;

        this.registerMessageHandlers();
    }

    /**
     * Create a NetworkPlayer for the given identity and register it in the manager.
     *
     * @param uuid the player's unique UUID
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
     * Installs cluster message handlers that deliver inter-server chat payloads to Bukkit players.
     *
     * <p>Registers handlers for:
     * <ul>
     *   <li>`SendMessage` — resolves a target by UUID and delivers a rich chat message to that player.</li>
     *   <li>`BroadcastMessage` — delivers a rich chat message to every online player.</li>
     *   <li>`SendActionbar` — resolves a target by UUID and delivers a parsed action bar message to that player.</li>
     * </ul>
     */
    private void registerMessageHandlers() {
        Cluster.getInstance().getMessagingService()
                .registerMessageHandler(SendMessage.class, message -> {
                    final var uuid = message.uuid();
                    final var player = Limbo.getInstance().getPlayer(uuid);

                    if (player == null) {
                        throw new UnregisteredPlayerException("tried to message unregistered player");
                    }

                    player.sendMessage(ColorUtils.parse(message.message()));
                });

        Cluster.getInstance().getMessagingService()
                .registerMessageHandler(SendActionbar.class, sendActionbar -> {

                    final var uuid = sendActionbar.uuid();
                    final var player = Limbo.getInstance().getPlayer(uuid);

                    if (player == null) {
                        throw new UnregisteredPlayerException("tried to message unregistered player");
                    }

                    player.sendActionBar(ColorUtils.parse(sendActionbar.message()));
                });
    }

    /**
     * Resolve the NetworkPlayer associated with the given Bukkit Player.
     *
     * @param player the Bukkit Player whose associated NetworkPlayer to retrieve
     * @return the NetworkPlayer associated with the player's UUID
     * @throws UnregisteredPlayerException if no NetworkPlayer is registered for the player's UUID
     */
    @NotNull
    public NetworkPlayer fromLimboPlayer(final Player player) {
        if (!this.networkPlayers.containsKey(player.getUniqueId())) {
            this.networkPlayers.put(player.getUniqueId(), new NetworkPlayer(
                    player.getUniqueId(),
                    player.getName(),
                    Cluster.getInstance().getServiceId()
            ));
        }

        return this.fromBukkitPlayer(player.getUniqueId());
    }

    /**
     * Resolve the NetworkPlayer for a Bukkit player's UUID.
     *
     * @param uuid the Bukkit player's UUID used to look up the NetworkPlayer
     * @return the NetworkPlayer mapped to the UUID, or {@code null} if no mapping exists
     * @throws UnregisteredPlayerException if the internal registry already contains the given UUID
     */
    @NotNull
    public NetworkPlayer fromBukkitPlayer(final UUID uuid) {
        if (!this.networkPlayers.containsKey(uuid)) {
            throw new UnregisteredPlayerException("tried to find a non existing player");
        }

        return this.networkPlayers.get(uuid);
    }

    /**
     * Locate a registered NetworkPlayer by username using a case-insensitive match.
     *
     * @param name the username to search for (case-insensitive)
     * @return the first matching NetworkPlayer, or `null` if no registered player matches
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