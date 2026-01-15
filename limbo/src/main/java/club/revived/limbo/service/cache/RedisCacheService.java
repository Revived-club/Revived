package club.revived.limbo.service.cache;

import com.google.gson.Gson;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.ScanParams;

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
     * @param host     the Redis server hostname or IP
     * @param port     the Redis server port
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
        CompletableFuture.runAsync(() -> {
            try (final var jedis = this.jedisPool.getResource()) {
                final var json = this.gson.toJson(t);
                jedis.set(key, json);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }, this.subServer);
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
        CompletableFuture.runAsync(() -> {
            try (final var jedis = this.jedisPool.getResource()) {
                final var json = this.gson.toJson(t);
                jedis.setex(key, seconds, json);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }, this.subServer);
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
        CompletableFuture.runAsync(() -> {
            try (final var jedis = this.jedisPool.getResource()) {
                final var json = this.gson.toJson(t);
                jedis.rpush(key, json);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }, this.subServer);
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
    public <T> CompletableFuture<List<T>> getAll(
            final String key,
            final Class<T> clazz
    ) {
        return CompletableFuture.supplyAsync(() -> {
            final var list = new ArrayList<T>();

            try (final var jedis = this.jedisPool.getResource()) {
                final var jsonList = jedis.lrange(key, 0, -1);

                for (final var json : jsonList) {
                    list.add(this.gson.fromJson(json, clazz));
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }

            return list;
        }, this.subServer);
    }

    /**
     * Deletes the given key from Redis.
     *
     * @return {@code true} if the key was removed, {@code false} otherwise.
     */
    @Override
    public CompletableFuture<Boolean> remove(
            final String key
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try (final var jedis = this.jedisPool.getResource()) {
                return jedis.del(key) > 0;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }, this.subServer);
    }

    /**
     * Removes occurrences of the given object from a Redis list stored under the specified key.
     * <p>
     * The object is serialized to JSON before comparison; matching list entries equal to that JSON
     * representation are removed according to Redis `LREM` semantics.
     *
     * @param key   the Redis list key
     * @param t     the object to remove (matched by its JSON serialization)
     * @param count number of occurrences to remove: >0 removes from head to tail up to `count`,
     *              <0 removes from tail to head up to `|count|`, 0 removes all occurrences
     * @throws RuntimeException if serialization or Redis access fails
     */
    @Override
    public <T> void removeFromList(
            final String key,
            final T t,
            final long count
    ) {
        CompletableFuture.runAsync(() -> {
            try (final var jedis = this.jedisPool.getResource()) {
                final var json = this.gson.toJson(t);
                jedis.lrem(key, count, json);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }, this.subServer);
    }

    @Override
    public void invalidateAll(final String param) {
        CompletableFuture.runAsync(() -> {
            var cursor = ScanParams.SCAN_POINTER_START;
            final var params = new ScanParams()
                    .match(param + ":*")
                    .count(1000);

            try (final var jedis = this.jedisPool.getResource()) {
                do {
                    final var result = jedis.scan(cursor, params);
                    final var keys = result.getResult();

                    if (!keys.isEmpty()) {
                        jedis.del(keys.toArray(new String[0]));
                    }

                    cursor = result.getCursor();
                } while (!cursor.equals(ScanParams.SCAN_POINTER_START));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }, this.subServer);
    }
}