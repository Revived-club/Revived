package club.revived.proxy.service.broker;

import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
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
     * Creates a RedisBroker and initializes the Redis connection pool used for publishing and subscribing.
     *
     * @param host     Redis server hostname or IP address
     * @param port     Redis server port
     * @param password Password for Redis authentication; an empty string indicates no authentication
     */
    public RedisBroker(
            final String host,
            final int port,
            final String password
    ) {
        this.jedisPool = this.connect(host, port, password);

        try (final var jedis = this.jedisPool.getResource()) {
            System.out.println("Checking connection...");
            System.out.println("Response " + jedis.ping());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Creates a RedisBroker connected to the given host and port using no password.
     *
     * @param host the Redis server hostname or IP
     * @param port the Redis server TCP port
     */
    public RedisBroker(
            final String host,
            final int port
    ) {
        this(host, port, "");
    }

    /**
     * Create a JedisPool connected to the specified Redis host and port.
     *
     * @param host     the Redis server hostname or IP address
     * @param port     the Redis server port
     * @param password the password for authenticating with Redis; empty string for no authentication
     * @return a configured {@link JedisPool} connected to the specified host and port using the provided password
     */
    @Override
    public JedisPool connect(
            final String host,
            final int port,
            final String password
    ) {
        try {
            final JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxIdle(20);
            config.setMaxTotal(50);
            config.setTestOnBorrow(true);
            config.setTestOnReturn(true);

            System.out.println("Connecting to Redis...");

            if (password.isEmpty()) {
                return new JedisPool(config, host, port, 0);
            } else {
                return new JedisPool(config, host, port, 0, password, false);
            }

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
      * Publishes the given message to the specified Redis topic after serializing it to JSON.
      *
      * <p>Any exceptions thrown during serialization or publish are caught and not propagated.</p>
      *
      * @param topic   the Redis channel to publish the message to
      * @param message the object to serialize to JSON and send to the topic
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
            throw new RuntimeException(e);
        }
     }

    /**
     * Subscribes to a Redis topic and delivers incoming JSON messages to the given handler.
     *
     * Subscription is performed asynchronously on the broker's executor; each received message
     * is deserialized to the provided type using Gson and passed to the handler. Exceptions
     * thrown while deserializing or handling a message are caught and suppressed.
     *
     * @param topic   the Redis channel/topic to subscribe to
     * @param type    the target class to deserialize incoming JSON messages into
     * @param handler callback invoked with each deserialized message
     */
    @Override
    public <T> void subscribe(
            final String topic,
            final Class<T> type,
            final MessageHandler<T> handler
    ) {
        System.out.println("Subscribing to redis with handler for " + type.getName());

        subServer.submit(() -> {
            try (final Jedis jedis = jedisPool.getResource()) {
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
                            throw new RuntimeException(e);
                        }
                    }
                }, topic);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}