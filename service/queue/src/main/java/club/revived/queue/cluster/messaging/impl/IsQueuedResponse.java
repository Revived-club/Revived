package club.revived.queue.cluster.messaging.impl;

import club.revived.queue.cluster.messaging.Request;
import club.revived.queue.cluster.messaging.Response;

import java.util.UUID;

/**
 * IsQueued
 *
 * @author yyuh
 * @since 09.01.26
 */
public record IsQueuedResponse(UUID uuid, boolean queued) implements Response {
}
