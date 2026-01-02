package club.revived.lobby.service.player;

import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.exception.UnregisteredPlayerException;
import club.revived.lobby.service.player.impl.SendMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This is an interesting Class
 *
 * @author yyuh
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
                    final var player = Bukkit.getPlayer(uuid);

                    if (player == null) {
                        throw new UnregisteredPlayerException("tried to message unregistered player");
                    }

                    player.sendRichMessage(message.message());
                });
    }

    @NotNull
    public NetworkPlayer fromBukkitPlayer(final Player player) {
        return this.fromBukkitPlayer(player.getUniqueId());
    }

    @NotNull
    public NetworkPlayer fromBukkitPlayer(final UUID uuid) {
        if (this.networkPlayers.containsKey(uuid)) {
            throw new UnregisteredPlayerException("tried to find a non existing player");
        }

        return this.networkPlayers.get(uuid);
    }

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
