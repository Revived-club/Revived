package club.revived.lobby.service.messaging;

import java.util.UUID;

public record Envelope(
        UUID correlationId, 
        String senderId, 
        String targetId, 
        String payloadType, 
        String payloadJson
) {
}
