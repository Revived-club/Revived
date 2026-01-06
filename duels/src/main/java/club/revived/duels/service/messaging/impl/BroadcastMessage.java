package club.revived.duels.service.messaging.impl;

import club.revived.duels.service.messaging.Message;

/**
 * BroadcastMessage
 *
 * @author yyuh
 * @since 06.01.26
 */
public record BroadcastMessage(String message) implements Message {
}
