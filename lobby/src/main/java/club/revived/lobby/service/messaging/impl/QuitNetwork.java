package club.revived.lobby.service.messaging.impl;

import club.revived.lobby.service.messaging.Message;

import java.util.UUID;

/**
 * QuitNetwork
 *
 * @author yyuh
 * @since 11.01.26
 */
public record QuitNetwork(UUID uuid) implements Message {
}
