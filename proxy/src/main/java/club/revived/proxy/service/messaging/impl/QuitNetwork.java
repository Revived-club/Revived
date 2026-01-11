package club.revived.proxy.service.messaging.impl;

import club.revived.proxy.service.messaging.Message;

import java.util.UUID;

/**
 * QuitNetwork
 *
 * @author yyuh
 * @since 11.01.26
 */
public record QuitNetwork(UUID uuid) implements Message {
}
