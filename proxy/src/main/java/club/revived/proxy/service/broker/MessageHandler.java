package club.revived.proxy.service.broker;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
@FunctionalInterface
public interface MessageHandler<T> {
    /**
     * Handle the given message.
     *
     * @param message the message to process
     */
    void handle(T message);
}