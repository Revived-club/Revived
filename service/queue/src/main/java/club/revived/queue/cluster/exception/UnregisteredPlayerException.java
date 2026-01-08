package club.revived.queue.cluster.exception;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public class UnregisteredPlayerException extends RuntimeException {
    /**
     * Creates a new UnregisteredPlayerException with the specified detail message.
     *
     * @param message the detail message describing why the player is considered unregistered
     */
    public UnregisteredPlayerException(String message) {
        super(message);
    }
}