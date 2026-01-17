package club.revived.duels.service.messaging.impl;

import java.util.UUID;

import club.revived.duels.service.messaging.Request;

public record IsDuelingRequest(
    UUID uuid) implements Request {

}
