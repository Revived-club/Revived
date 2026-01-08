package club.revived.queue.cluster.messaging.impl;

import club.revived.queue.cluster.messaging.Response;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public record WhereIsProxyResponse(String proxy) implements Response {
}
