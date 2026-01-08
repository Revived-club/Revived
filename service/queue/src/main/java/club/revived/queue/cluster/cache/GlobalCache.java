package club.revived.queue.cluster.cache;

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
     * Store a value in the global cache under the specified key, replacing any existing entry.
     *
     * @param key the cache key to associate with the value
     * @param t   the value to store
     */
    <T> void set(
            final String key,
            final T t
    );

    /**
     * Adds a value to the cache entry identified by the given key while preserving any existing entries.
     * For example, if the entry is a collection, the value will be appended to that collection.
     *
     * @param key the cache key under which the value will be added
     * @param t   the value to add to the cache entry
     */
    <T> void push(
            final String key,
            final T t
    );

    /**
     * Remove the entry for the given cache key.
     *
     * @param key the cache key to remove
     * @return {@code true} if the key existed and was removed, {@code false} otherwise
     */
    CompletableFuture<Boolean> remove(
            final String key
    );

    /**
     * Removes occurrences of a value from the list stored under the specified cache key.
     *
     * @param key   the cache key containing a list
     * @param t     the value to remove from the list
     * @param count the maximum number of occurrences to remove; 0 removes all occurrences
     */
    <T> void removeFromList(
            final String key,
            final T t,
            final long count
    );

    /**
     * Retrieve all values stored under the given cache key as a list of the specified type.
     *
     * @param key   the cache key whose associated values should be returned
     * @param clazz the class of the elements to decode or cast the stored values to
     * @return      a list of values of type `T`; empty if no values are associated with the key
     */
    <T> CompletableFuture<List<T>> getAll(
            final String key,
            final Class<T> clazz
    );

    /**
     * Store a value under the specified key with a time-to-live.
     *
     * @param <T>     the type of the stored value
     * @param key     the cache key
     * @param t       the value to store
     * @param seconds time-to-live in seconds; the entry expires and is removed after this duration
     */
    <T>

    void setEx(
            final String key,
            final T t,
            final long seconds
    );

    /**
         * Open a connection to the cache service at the given host and port using the provided password.
         *
         * @param host     hostname or IP address of the cache service
         * @param port     TCP port of the cache service
         * @param password authentication password for the connection
         * @param <P>      the connection or client implementation type returned
         * @return the established connection or client instance
         */
    <P> P connect(
            final String host,
            final int port,
            final String password
    );
}