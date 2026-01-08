package club.revived.queue.cluster.broker;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public interface MessageBroker {
    /**
 * Publish a message to the specified topic.
 *
 * @param topic   the topic name to publish the message to
 * @param message the payload to publish
 */
<T> void publish(String topic, T message);

    /**
 * Register a handler to receive messages published to the specified topic using the provided runtime payload type.
 *
 * The handler will be invoked for messages whose payloads match the given `type`.
 *
 * @param topic the topic to subscribe to
 * @param type  the runtime class of the expected message payload
 * @param handler the handler that processes incoming messages of type `T`
 */
<T> void subscribe(String topic, Class<T> type, MessageHandler<T> handler);

    /**
     * Establishes a connection to a message broker at the given host and port using the provided password.
     *
     * @param host     the broker hostname or IP address
     * @param port     the broker port number
     * @param password the authentication password to use for the connection (may be null or empty if not required)
     * @param <P>      the concrete connection or client type returned
     * @return         a connection or client instance of type `P` representing the established connection
     */
    <P> P connect(
            final String host,
            final int port,
            final String password
    );
}