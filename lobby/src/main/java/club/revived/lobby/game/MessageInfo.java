package club.revived.lobby.game;

import java.util.UUID;

public record MessageInfo(
        UUID sender,
        String content,
        long timestamp
) {}
