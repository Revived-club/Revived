package club.revived.lobby.service.broker;

import com.google.gson.Gson;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class RedisBroker implements MessageBroker {

    private final JedisPool jedisPool;
    private final ExecutorService subServer = Executors.newVirtualThreadPerTaskExecutor();
    private final Gson gson = new Gson();

    public RedisBroker(
            final String host,
            final int port,
            final String password
    ) {
        this.jedisPool = this.connect(host, port, password);
    }

    public RedisBroker(
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
    public <T> void publish(
            final String topic,
            final T message
    ) {
        try (final var jedis = jedisPool.getResource()) {
            final String json = this.gson.toJson(message);
            jedis.publish(topic, json);
        } catch (final Exception e) {
            // TODO: Log
        }
     }

    @Override
    public <T> void subscribe(
            final String topic,
            final Class<T> type,
            final MessageHandler<T> handler
    ) {
        subServer.submit(() -> {
            try (final var jedis = jedisPool.getResource()) {
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(
                            final String channel,
                            final String message
                    ) {
                        try {
                            final T obj = gson.fromJson(message, type);
                            handler.handle(obj);
                        } catch (final Exception e) {
                            // TODO: Log
                        }
                    }
                });
            }
        }, topic);
    }
}
