package club.revived.duels.service.broker;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public interface MessageBroker {
    <T> void publish(String topic, T message);

    <T> void subscribe(String topic, Class<T> type, MessageHandler<T> handler);

    <P> P connect(
            final String host,
            final int port,
            final String password
    );
}
