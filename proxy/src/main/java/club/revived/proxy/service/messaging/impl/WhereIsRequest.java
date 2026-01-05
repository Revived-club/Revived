package club.revived.proxy.service.messaging.impl;

import club.revived.proxy.service.messaging.Request;

import java.util.UUID;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public record WhereIsRequest(UUID uuid) implements Request {
}
