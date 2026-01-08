package club.revived.queue.cluster.broker;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
@FunctionalInterface
public interface MessageHandler<T> {
    /**
 * Handle a message of type T.
 *
 * @param message the message to be processed by this handler
 */
void handle(T message);
}