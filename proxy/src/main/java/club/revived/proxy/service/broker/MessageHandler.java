package club.revived.proxy.service.broker;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
@FunctionalInterface
public interface MessageHandler<T> {
    void handle(T message);
}
