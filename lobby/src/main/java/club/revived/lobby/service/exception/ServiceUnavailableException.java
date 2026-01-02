package club.revived.lobby.service.exception;

public final class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(final String message) {
        super(message);
    }
}