package club.revived.lobby.service.heartbeat;

import club.revived.lobby.service.cluster.OnlinePlayer;
import club.revived.lobby.service.cluster.ServiceType;

import java.util.List;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public record Heartbeat(
        long timestamp,
        ServiceType serviceType,
        String id,
        int playerCount,
        List<OnlinePlayer> onlinePlayers,
        String serverIp
) {
}
