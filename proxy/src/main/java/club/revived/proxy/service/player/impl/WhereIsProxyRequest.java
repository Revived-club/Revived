package club.revived.proxy.service.player.impl;

import club.revived.proxy.service.messaging.Request;

import java.util.UUID;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public record WhereIsProxyRequest(UUID uuid) implements Request {
}
