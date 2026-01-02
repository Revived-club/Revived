package club.revived.lobby.service.player.impl;

import club.revived.lobby.service.messaging.Message;

import java.util.UUID;

/**
 * Sends a message to the player
 *
 * @author yyuh
 */
public record SendMessage(UUID uuid, String message) implements Message {
}
