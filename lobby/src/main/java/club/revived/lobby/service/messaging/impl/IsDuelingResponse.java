package club.revived.lobby.service.messaging.impl;

import java.util.UUID;

import club.revived.lobby.service.messaging.Response;

public record IsDuelingResponse(
    UUID uuid,
    boolean dueling) implements Response {

}
