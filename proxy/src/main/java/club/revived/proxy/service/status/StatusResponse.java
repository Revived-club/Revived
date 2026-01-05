package club.revived.proxy.service.status;

import club.revived.proxy.service.messaging.Response;
import club.revived.proxy.service.status.ServiceStatus;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public record StatusResponse(ServiceStatus status) implements Response {
}
