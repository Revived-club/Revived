package club.revived.lobby.service.status;

import club.revived.lobby.service.messaging.Response;

public record StatusResponse(ServiceStatus status) implements Response {
}
