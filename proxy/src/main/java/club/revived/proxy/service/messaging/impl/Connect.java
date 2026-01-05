package club.revived.proxy.service.messaging.impl;

import club.revived.proxy.service.messaging.Message;

import java.util.UUID;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public record Connect(UUID uuid, String server) implements Message {
}
