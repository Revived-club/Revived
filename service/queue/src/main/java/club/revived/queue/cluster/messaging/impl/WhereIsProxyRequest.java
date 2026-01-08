package club.revived.queue.cluster.messaging.impl;

import club.revived.queue.cluster.messaging.Request;

import java.util.UUID;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public record WhereIsProxyRequest(UUID uuid) implements Request {
}
