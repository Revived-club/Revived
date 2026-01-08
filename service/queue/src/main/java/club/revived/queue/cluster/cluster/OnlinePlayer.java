package club.revived.queue.cluster.cluster;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public record OnlinePlayer(
        @NotNull UUID uuid,
        @NotNull String username,
        @NotNull String currentServer,
        int ping,
        @NotNull String skinBase64,
        @NotNull String signing
) {}
