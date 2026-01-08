package club.revived.queue.cluster.exception;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class ServiceUnavailableException extends RuntimeException {
    /**
     * Creates a new ServiceUnavailableException with the specified detail message.
     *
     * @param message the detail message describing the unavailable service
     */
    public ServiceUnavailableException(final String message) {
        super(message);
    }
}