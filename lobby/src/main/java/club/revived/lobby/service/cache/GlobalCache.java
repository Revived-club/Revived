package club.revived.lobby.service.cache;

public interface GlobalCache {

    <T> T get(Class<T> clazz, String key);
    <T> void set(String key, T t);
    <P> P connect(
            final String host,
            final int port,
            final String password
    );
}
