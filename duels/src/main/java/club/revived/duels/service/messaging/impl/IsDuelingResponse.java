package club.revived.duels.service.messaging.impl;

import java.util.UUID;

import club.revived.duels.service.messaging.Response;

public record IsDuelingResponse(
    UUID uuid,
    String gameId,
    boolean dueling) implements Response {

}
