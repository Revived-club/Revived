package club.revived.duels.service.status;

import club.revived.lobby.service.messaging.Response;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public record StatusResponse(ServiceStatus status) implements Response {
}
