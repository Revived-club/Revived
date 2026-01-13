package club.revived.lobby.service.cache;

import com.google.gson.Gson;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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

    private static final String LIST_PREFIX = "list:";
    private static final String ID_PREFIX   = "obj:";

    public RedisCacheService(
            final String host,
            final int port,
            final String password
    ) {
        this.jedisPool = this.connect(host, port, password);
    }

    public RedisCacheService(
            final String host,
            final int port
    ) {
        this.jedisPool = this.connect(host, port, "");
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
     * @return      an instance of {@code clazz} deserialized from the stored JSON, or {@code null} if the key does not exist
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
     * Stores the given value at the specified Redis key and sets its expiration.
     *
     * @param key     the Redis key under which to store the value
     * @param t       the value to store
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
     * Deletes the value stored at the given Redis key from the cache.
     *
     * @param key the cache key to delete
     * @return `true` if the key was removed, `false` otherwise
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

    @Override
    public <T> void push(
            final String listKey,
            final String id,
            final T t
    ) {
        try (final var jedis = this.jedisPool.getResource()) {
            final String objectKey = ID_PREFIX + id;
            final String listRedisKey = LIST_PREFIX + listKey;

            jedis.set(objectKey, this.gson.toJson(t));
            jedis.rpush(listRedisKey, id);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void pushEx(
            final String listKey,
            final String id,
            final T t,
            final long ttlSeconds
    ) {
        try (final var jedis = this.jedisPool.getResource()) {
            final String objectKey = ID_PREFIX + id;
            final String listRedisKey = LIST_PREFIX + listKey;

            jedis.setex(objectKey, ttlSeconds, this.gson.toJson(t));
            jedis.rpush(listRedisKey, id);
            jedis.expire(listRedisKey, ttlSeconds);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> CompletableFuture<List<T>> getAll(
            final String listKey,
            final Class<T> clazz
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try (final var jedis = this.jedisPool.getResource()) {
                final String listRedisKey = LIST_PREFIX + listKey;

                final List<String> ids = jedis.lrange(listRedisKey, 0, -1);
                if (ids.isEmpty()) {
                    return List.of();
                }

                try (final var pipeline = jedis.pipelined()) {
                    ids.forEach(id -> pipeline.get(ID_PREFIX + id));
                    final List<Object> results = pipeline.syncAndReturnAll();

                    return results.stream()
                            .filter(Objects::nonNull)
                            .map(obj -> gson.fromJson(obj.toString(), clazz))
                            .collect(Collectors.toList());
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }, this.subServer);
    }



    @Override
    public <T> CompletableFuture<T> getById(
            final Class<T> clazz,
            final String id
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try (final var jedis = this.jedisPool.getResource()) {
                final String json = jedis.get(ID_PREFIX + id);
                return json == null ? null : gson.fromJson(json, clazz);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }, this.subServer);
    }

    @Override
    public <T> void update(
            final String id,
            final T t
    ) {
        try (final var jedis = this.jedisPool.getResource()) {
            jedis.set(ID_PREFIX + id, gson.toJson(t));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeFromList(
            final String listKey,
            final String id,
            final long count
    ) {
        try (final var jedis = this.jedisPool.getResource()) {
            jedis.lrem(LIST_PREFIX + listKey, count, id);
            jedis.del(ID_PREFIX + id);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}