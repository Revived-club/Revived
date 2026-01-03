package club.revived.duels.service.message;

import club.revived.lobby.service.messaging.Message;

import java.util.UUID;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public record BotDuelStart(
        UUID uuid,
        String type,
        int rounds,
        String difficulty
) implements Message {
}
