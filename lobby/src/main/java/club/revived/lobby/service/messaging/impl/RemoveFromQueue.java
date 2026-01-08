package club.revived.lobby.service.messaging.impl;

import club.revived.lobby.service.messaging.Message;

import java.util.UUID;

/**
 * RemoveFromQueue
 *
 * @author yyuh - DL
 * @since 1/8/26
 */
public record RemoveFromQueue(UUID uuid) implements Message {
}
