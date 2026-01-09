package club.revived.lobby.service.messaging.impl;

import club.revived.lobby.service.messaging.Response;

import java.util.UUID;

/**
 * IsQueued
 *
 * @author yyuh
 * @since 09.01.26
 */
public record IsQueuedResponse(UUID uuid, boolean queued) implements Response {
}
