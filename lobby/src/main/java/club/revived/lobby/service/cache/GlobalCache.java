package club.revived.lobby.service.cache;

import java.util.concurrent.CompletableFuture;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public interface GlobalCache {

    <T> CompletableFuture<T> get(Class<T> clazz, String key);
    <T> void set(String key, T t);
    <P> P connect(
            final String host,
            final int port,
            final String password
    );
}
