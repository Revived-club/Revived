package club.revived.lobby.service.message;

import club.revived.lobby.service.messaging.Message;

import java.util.UUID;

public record BotDuelStart(
        UUID uuid,
        String type,
        int rounds,
        String difficulty
) implements Message {
}
