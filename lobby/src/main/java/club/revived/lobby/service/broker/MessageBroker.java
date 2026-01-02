package club.revived.lobby.service.broker;

public interface MessageBroker {
    <T> void publish(String topic, T message);

    <T> void subscribe(String topic, Class<T> type, MessageHandler<T> handler);

    <P> P connect(
            final String host,
            final int port,
            final String password
    );
}
