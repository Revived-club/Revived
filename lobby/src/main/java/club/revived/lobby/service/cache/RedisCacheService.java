package club.revived.lobby.service.cache;

import com.google.gson.Gson;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RedisCacheService implements GlobalCache {

    private final JedisPool jedisPool;
    private final ExecutorService subServer = Executors.newVirtualThreadPerTaskExecutor();
    private final Gson gson = new Gson();

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

        return new JedisPool(config, host, port, 0, password, false);
    }

    @Override
    public <T> T get(
            final Class<T> clazz,
            final String key
    ) {
        try (final var jedis = this.jedisPool.getResource()) {
             final var string = jedis.get(key);

            return this.gson.fromJson(string, clazz);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

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
}
