package club.revived.lobby.service.messaging.impl;

import club.revived.lobby.service.messaging.Response;

/**
 * QueuedAmount
 *
 * @author yyuh
 * @since 14.01.26
 */
public record QueuedAmountResponse(int amount) implements Response {
}
