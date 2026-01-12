package club.revived.proxy.service.broker;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public interface MessageBroker {
    /**
     * Publishes a typed message to a named topic so registered subscribers can receive it.
     *
     * @param topic   the destination topic identifier to which the message will be published
     * @param message the payload to deliver to subscribers of the topic
     */
    <T> void publish(String topic, T message);

    /**
     * Subscribes a handler to receive messages of the specified type published to the given topic.
     *
     * @param topic   the topic name to subscribe to
     * @param type    the class of messages to accept; only messages assignable to this type will be delivered
     * @param handler callback invoked for each received message of the specified type on the topic
     */
    <T> void subscribe(String topic, Class<T> type, MessageHandler<T> handler);

    /**
     * Establishes a connection to a message broker at the specified host and port using the provided password.
     *
     * @param host     the broker hostname or IP address
     * @param port     the broker TCP port
     * @param password the credential used to authenticate with the broker
     * @param <P>      the type representing the established connection or session handle
     * @return the established connection or session handle of type {@code P}
     */
    <P> P connect(
            final String host,
            final int port,
            final String password
    );
}