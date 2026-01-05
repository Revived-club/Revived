package club.revived.duels.service.broker;

import com.google.gson.Gson;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class RedisBroker implements MessageBroker {

    private final JedisPool jedisPool;
    private final ExecutorService subServer = Executors.newVirtualThreadPerTaskExecutor();
    private final Gson gson = new Gson();

    /**
     * Create a RedisBroker connected to the specified Redis instance.
     *
     * Initializes the broker's internal JedisPool using the provided connection parameters.
     *
     * @param host     the Redis server hostname or IP address
     * @param port     the Redis server port
     * @param password the Redis authentication password; use an empty string if no password is required
     */
    public RedisBroker(
            final String host,
            final int port,
            final String password
    ) {
        this.jedisPool = this.connect(host, port, password);
    }

    /**
     * Creates a RedisBroker connected to the given host and port using no password.
     *
     * @param host the Redis server host
     * @param port the Redis server port
     */
    public RedisBroker(
            final String host,
            final int port
    ) {
        this.jedisPool = this.connect(host, port, "");
    }

    /**
     * Create and return a configured JedisPool connected to the specified Redis instance.
     *
     * @param host     the Redis server host
     * @param port     the Redis server port
     * @param password the password for authentication; provide an empty string for no password
     * @return         a configured {@link JedisPool} connected to the given host and port
     */
    @Override
    public JedisPool connect(
            final String host,
            final int port,
            final String password
    ) {
        final JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(20);
        config.setMaxTotal(50);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);

        if (password.isEmpty()) {
            return new JedisPool(config, host, port, 0);
        } else {
            return new JedisPool(config, host, port, 0, password, false);
        }
    }

    /**
      * Publishes a message to a Redis topic after serializing it to JSON.
      *
      * @param topic   the Redis channel name to publish to
      * @param message the message object to serialize as JSON and publish
      */
     @Override
    public <T> void publish(
            final String topic,
            final T message
    ) {
        try (final var jedis = jedisPool.getResource()) {
            final String json = this.gson.toJson(message);
            jedis.publish(topic, json);
        } catch (final Exception e) {
            // TODO: Log
        }
     }

    /**
     * Subscribes to a Redis topic and dispatches each received JSON message (deserialized to the given type) to the provided handler.
     *
     * Messages are deserialized using the broker's Gson instance; any exception thrown while handling a message is caught and suppressed.
     *
     * @param topic   the Redis channel to subscribe to
     * @param type    the class to deserialize incoming JSON messages into
     * @param handler the handler invoked for each deserialized message
     */
    @Override
    public <T> void subscribe(
            final String topic,
            final Class<T> type,
            final MessageHandler<T> handler
    ) {
        subServer.submit(() -> {
            try (final var jedis = jedisPool.getResource()) {
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(
                            final String channel,
                            final String message
                    ) {
                        try {
                            final T obj = gson.fromJson(message, type);
                            handler.handle(obj);
                        } catch (final Exception e) {
                            // TODO: Log
                        }
                    }
                });
            }
        }, topic);
    }
}