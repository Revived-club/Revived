package club.revived.lobby.service.messaging;

import java.util.UUID;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public record Envelope(
        UUID correlationId, 
        String senderId, 
        String targetId, 
        String payloadType, 
        String payloadJson
) {
}
