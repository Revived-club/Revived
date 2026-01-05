package club.revived.lobby.service.messaging.impl;

import club.revived.lobby.service.messaging.Request;

import java.util.UUID;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public record WhereIsProxyRequest(UUID uuid) implements Request {
}
