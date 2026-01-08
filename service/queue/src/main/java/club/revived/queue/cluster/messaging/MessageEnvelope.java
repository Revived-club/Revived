package club.revived.queue.cluster.messaging;

import java.util.UUID;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public record MessageEnvelope(
        UUID correlationId, 
        String senderId, 
        String targetId, 
        String payloadType, 
        String payloadJson
) {
}
