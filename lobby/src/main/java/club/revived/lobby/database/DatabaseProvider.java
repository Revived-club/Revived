package club.revived.lobby.database;

import java.util.Optional;
import java.util.UUID;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public interface DatabaseProvider<T> {

    void start();
    void save(T t);
    Optional<T> get(UUID uuid);
}