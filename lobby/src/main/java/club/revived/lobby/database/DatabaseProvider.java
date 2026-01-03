package club.revived.lobby.database;

import org.jetbrains.annotations.NotNull;

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
    @NotNull Optional<T> get(String key);
}