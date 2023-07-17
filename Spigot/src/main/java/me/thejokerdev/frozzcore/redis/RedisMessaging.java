package me.thejokerdev.frozzcore.redis;

import me.thejokerdev.frozzcore.SpigotMain;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RedisMessaging {
    private final SpigotMain plugin;
    private final ExecutorService executorService;

    public RedisMessaging(SpigotMain plugin) {
        this.plugin = plugin;
        executorService = Executors.newCachedThreadPool();
    }

    public void subscribe(String channel, Consumer<String> consumer) {
        executorService.submit(() -> {
            try (Jedis jedis = plugin.getRedis().getPool().getResource()) {
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        consumer.accept(message);
                    }
                }, channel);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }
}
