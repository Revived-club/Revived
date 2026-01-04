package club.revived.duels.service.cache;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public interface GlobalCache {

    /**
     * Retrieve a cached value by its key and expected type.
     *
     * @param <T>   the expected type of the cached value
     * @param clazz the Class object representing the expected type for decoding/casting
     * @param key   the cache key
     * @return the cached value of type T, or {@code null} if no value is associated with the key
     */
    <T> CompletableFuture<T> get(
            final Class<T> clazz,
            final String key
    );

    /**
     * Stores a value in the global cache under the specified key.
     *
     * @param key the cache key to associate with the value
     * @param t   the value to store
     */
    <T> void set(
            final String key,
            final T t
    );

    <T> void push(
            final String key,
            final T t
    );

    <T> List<T> getAll(
            final String key,
            final Class<T> clazz
    );

    /**
     * Stores a value in the global cache under the specified key and sets an expiration time.
     *
     * @param key     the cache key under which the value will be stored
     * @param t       the value to store
     * @param seconds the time-to-live for the stored value, in seconds
     */
    <T>

    void setEx(
            final String key,
            final T t,
            final long seconds
    );

    /**
     * Establishes a connection to the cache service at the specified host and port using the provided password.
     *
     * @param host     the hostname or IP address of the cache service
     * @param port     the TCP port of the cache service
     * @param password the authentication password to use for the connection
     * @param <P>      the type of the returned connection or client
     * @return a connection or client instance of type P representing the established connection
     */
    <P> P connect(
            final String host,
            final int port,
            final String password
    );
}