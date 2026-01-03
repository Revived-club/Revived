package club.revived.lobby.service.player.impl;

import club.revived.lobby.service.messaging.Message;

import java.util.UUID;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public record Connect(UUID uuid, String server) implements Message {
}
