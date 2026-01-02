package club.revived.lobby.service.cluster;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record OnlinePlayer(
        @NotNull UUID uuid,
        @NotNull String username,
        @NotNull String currentServer
) {}
