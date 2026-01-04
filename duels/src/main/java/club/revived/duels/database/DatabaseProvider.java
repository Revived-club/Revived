package club.revived.duels.database;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public interface DatabaseProvider<T> {

    /**
     * Initializes and prepares the database provider for use.
     * <p>
     * Implementations perform any startup or resource initialization required so
     * subsequent calls to the provider's methods operate correctly.
     */
    void start();

    /**
     * Persists the given entity to the database.
     *
     * @param t the entity to persist
     */
    void save(T t);

    /**
     * Retrieves the stored value associated with the given key.
     *
     * @param key the unique string identifier for the stored object
     * @return an Optional containing the object if found, or an empty Optional if no value exists for the key
     */
    @NotNull Optional<T> get(String key);

    /**
 * Retrieves all stored entities.
 *
 * @return a non-null List containing every stored entity, or an empty list if none are stored
 */
@NotNull List<T> getAll();
}