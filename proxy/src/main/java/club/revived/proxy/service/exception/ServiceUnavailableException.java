package club.revived.proxy.service.exception;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class ServiceUnavailableException extends RuntimeException {
    /**
     * Create a new ServiceUnavailableException with the specified detail message.
     *
     * @param message a descriptive detail message explaining why the service is unavailable
     */
    public ServiceUnavailableException(final String message) {
        super(message);
    }
}