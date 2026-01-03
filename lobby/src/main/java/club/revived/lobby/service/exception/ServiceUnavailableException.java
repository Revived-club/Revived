package club.revived.lobby.service.exception;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(final String message) {
        super(message);
    }
}