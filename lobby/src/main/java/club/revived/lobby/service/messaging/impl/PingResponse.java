package club.revived.lobby.service.messaging.impl;

import club.revived.lobby.service.messaging.Response;

/**
 * PingResponse
 *
 * @author yyuh
 * @since 06.01.26
 */
public record PingResponse(String serverId) implements Response {
}
