package club.revived.duels.service.cache;

import com.google.gson.Gson;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class RedisCacheService implements GlobalCache {

    private final JedisPool jedisPool;
    private final ExecutorService subServer = Executors.newVirtualThreadPerTaskExecutor();
    private final Gson gson = new Gson();

    /**
     * Creates a RedisCacheService configured to connect to a Redis instance at the given host and port using the provided password.
     *
     * @param host the Redis server hostname or IP
     * @param port the Redis server port
     * @param password the authentication password for the Redis server (empty string if none)
     */
    public RedisCacheService(
            final String host,
            final int port,
            final String password
    ) {
        this.jedisPool = this.connect(host, port, password);
    }

    /**
     * Creates a RedisCacheService connected to the specified Redis host and port using an empty password.
     *
     * @param host the Redis server hostname or IP address
     * @param port the Redis server port
     */
    public RedisCacheService(
            final String host,
            final int port
    ) {
        this(host, port, "");
    }

    /**
     * Create and return a configured JedisPool connected to the specified Redis server.
     *
     * @param host     the Redis server hostname or IP address
     * @param port     the Redis server port
     * @param password the password for Redis authentication (may be empty)
     * @return a configured JedisPool instance connected to the given host and port using the provided password
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
     * Retrieve and deserialize a value stored in Redis for the given key.
     *
     * @param <T>   the expected return type
     * @param clazz the class to deserialize the stored JSON into
     * @param key   the Redis key to read the value from
     * @return an instance of {@code clazz} deserialized from the stored JSON, or {@code null} if the key does not exist
     * @throws RuntimeException if an error occurs while accessing Redis or deserializing the value
     */
    @Override
    public <T> CompletableFuture<T> get(
            final Class<T> clazz,
            final String key
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try (final var jedis = this.jedisPool.getResource()) {
                final var string = jedis.get(key);

                return this.gson.fromJson(string, clazz);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Stores the given object in Redis under the provided key as a JSON string.
     *
     * @param key the Redis key to set
     * @param t   the value to serialize to JSON and store
     * @throws RuntimeException if serialization or the Redis operation fails
     */
    @Override
    public <T> void set(
            final String key,
            final T t
    ) {
        try (final var jedis = this.jedisPool.getResource()) {
            final var json = this.gson.toJson(t);
            jedis.set(key, json);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Store a value in Redis under the given key with an expiration.
     *
     * @param key     the Redis key to set
     * @param t       the value to serialize and store
     * @param seconds expiration time in seconds
     * @throws RuntimeException if serialization or the Redis operation fails
     */
    @Override
    public <T> void setEx(
            final String key,
            final T t,
            final long seconds
    ) {
        try (final var jedis = this.jedisPool.getResource()) {
            final var json = this.gson.toJson(t);
            jedis.setex(key, seconds, json);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Appends the JSON-serialized form of the given object to the end of the Redis list stored at the specified key.
     *
     * @param key the Redis list key
     * @param t   the object to serialize and append to the list
     * @throws RuntimeException if serialization or the Redis operation fails
     */
    @Override
    public <T> void push(
            final String key,
            final T t
    ) {
        try (final var jedis = this.jedisPool.getResource()) {
            final var json = this.gson.toJson(t);
            jedis.rpush(key, json);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve and deserialize all elements of the Redis list at the given key.
     *
     * @param key   the Redis list key to read
     * @param clazz the target class to deserialize each list element into
     * @return a list of deserialized elements in list order (head to tail); empty if the key does not exist or has no elements
     * @throws RuntimeException if a Redis access or JSON deserialization error occurs
     */
    @Override
    public <T> List<T> getAll(
            final String key,
            final Class<T> clazz
    ) {
        final var list = new ArrayList<T>();

        try (final var jedis = this.jedisPool.getResource()) {
            final List<String> jsonList = jedis.lrange(key, 0, -1);

            for (final var json : jsonList) {
                final var t = this.gson.fromJson(json, clazz);
                list.add(t);
            }

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}