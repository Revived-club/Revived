package club.revived.lobby.service.player.impl;

import club.revived.lobby.service.messaging.Request;

import java.util.UUID;

public record WhereIsRequest(UUID uuid) implements Request {
}
