package club.revived.lobby.service.player;

import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.exception.UnregisteredPlayerException;
import club.revived.lobby.service.messaging.impl.BroadcastMessage;
import club.revived.lobby.service.messaging.impl.SendMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(PlayerManager.class);
    private final Map<UUID, NetworkPlayer> networkPlayers = new ConcurrentHashMap<>();

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
        this.networkPlayers.put(
                networkPlayer.getUuid(),
                networkPlayer
        );
    }

    private void registerMessageHandlers() {
        Cluster.getInstance().getMessagingService()
                .registerMessageHandler(SendMessage.class, message -> {
                    System.out.println("[HANDLER] Sending chat message to " + message.uuid());

                    final var uuid = message.uuid();
                    final var player = Bukkit.getPlayer(uuid);

                    if (player == null) {
                        throw new UnregisteredPlayerException("tried to message unregistered player");
                    }

                    player.sendRichMessage(message.message());
                });

        Cluster.getInstance().getMessagingService()
                .registerMessageHandler(BroadcastMessage.class, message -> {
                    for (final var player : Bukkit.getOnlinePlayers()) {
                        player.sendRichMessage(message.message());
                    }
                });
    }

    @NotNull
    public NetworkPlayer fromBukkitPlayer(final Player player) {
        return this.fromBukkitPlayer(player.getUniqueId());
    }

    /**
     * Retrieve the NetworkPlayer associated with the given Bukkit player's UUID.
     *
     * @param uuid the Bukkit player's UUID used to look up the NetworkPlayer
     * @return the NetworkPlayer mapped to the UUID, or {@code null} if no mapping exists
     * @throws UnregisteredPlayerException if the internal registry contains the given UUID
     */
    @NotNull
    public NetworkPlayer fromBukkitPlayer(final UUID uuid) {
        if (!this.networkPlayers.containsKey(uuid)) {
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