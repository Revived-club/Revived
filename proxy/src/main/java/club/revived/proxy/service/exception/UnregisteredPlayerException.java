package club.revived.proxy.service.exception;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public class UnregisteredPlayerException extends RuntimeException {
    /**
     * Constructs an UnregisteredPlayerException with the specified detail message.
     *
     * @param message the detail message explaining why the player is considered unregistered
     */
    public UnregisteredPlayerException(String message) {
        super(message);
    }
}