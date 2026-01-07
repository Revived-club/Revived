package club.revived.lobby.service.cache;

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
    <T> CompletableFuture<T> get(Class<T> clazz, String key);

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

    /**
     * Store a value under the specified key with a time-to-live.
     *
     * @param key     the cache key to store the value under
     * @param t       the value to store
     * @param seconds the time-to-live in seconds after which the entry expires
     */
    <T> void setEx(
            final String key,
            final T t,
            final long seconds
    );

    /**
     * Adds the given value to the cache entry identified by the specified key, preserving any existing entries (for example, appending to a collection stored at that key).
     *
     * @param key the cache key under which the value will be added
     * @param t   the value to add to the cache entry
     */
    <T> void push(
            final String key,
            final T t
    );

    /**
     * Retrieve all values stored under the given cache key as a list of the specified type.
     *
     * @param key   the cache key whose associated values should be returned
     * @param clazz the class of elements to decode or cast the stored values to
     * @return a list of values of type T; an empty list if no values are associated with the key
     */
    <T> CompletableFuture<List<T>> getAll(
            final String key,
            final Class<T> clazz
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

    /**
     * Removes the given key and its associated value(s) from the cache.
     *
     * @param key the cache key to remove
     * @return `true` if the key was removed, `false` otherwise
     */
    CompletableFuture<Boolean> remove(
            final String key
    );

    /**
     * Removes occurrences of a value from the list stored at the specified key.
     *
     * @param key   the list key
     * @param t     the value to remove
     * @param count the number of occurrences to remove; 0 removes all occurrences
     */
    <T> void removeFromList(
            final String key,
            final T t,
            final long count
    );
}