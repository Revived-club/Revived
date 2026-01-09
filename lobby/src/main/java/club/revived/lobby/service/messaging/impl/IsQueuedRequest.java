package club.revived.lobby.service.messaging.impl;

import club.revived.lobby.service.messaging.Request;

import java.util.UUID;

/**
 * IsQueuedRequest
 *
 * @author yyuh
 * @since 09.01.26
 */
public record IsQueuedRequest(UUID uuid) implements Request {
}
