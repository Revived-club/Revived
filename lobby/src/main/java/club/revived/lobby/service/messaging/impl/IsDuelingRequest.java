package club.revived.lobby.service.messaging.impl;

import java.util.UUID;

import club.revived.lobby.service.messaging.Request;

public record IsDuelingRequest(
    UUID uuid) implements Request {

}
