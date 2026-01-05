package club.revived.proxy.service.exception;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public class UnregisteredPlayerException extends RuntimeException {
    public UnregisteredPlayerException(String message) {
        super(message);
    }
}
