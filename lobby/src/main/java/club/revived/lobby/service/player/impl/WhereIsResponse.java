package club.revived.lobby.service.player.impl;

import club.revived.lobby.service.messaging.Response;

public record WhereIsResponse(String server) implements Response {
}
