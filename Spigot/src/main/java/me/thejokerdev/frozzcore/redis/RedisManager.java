package me.thejokerdev.frozzcore.redis;

import me.thejokerdev.frozzcore.SpigotMain;
import redis.clients.jedis.Jedis;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RedisManager {
    private final SpigotMain plugin;
    
    public RedisManager(SpigotMain plugin) {
        this.plugin = plugin;
    }

    public void getIfPresent(String key, Consumer<String> consumer) {
        new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, 5000, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(50000)).submit(() -> {
            try (Jedis jedis = plugin.getRedis().getPool().getResource()) {
                consumer.accept(jedis.get(key));
            }
        });
    }
    public String get(String key) {
        try (Jedis jedis = plugin.getRedis().getPool().getResource()) {
            return jedis.get(key);
        }
    }
    public String getDel(String key) {
        try (Jedis jedis = plugin.getRedis().getPool().getResource()) {
            return jedis.getDel(key);
        }
    }
    public boolean hasDel(String key) {
        return Optional.ofNullable(getDel(key)).isPresent();
    }
    public boolean has(String key) {
        return get(key) != null;
    }
    public Optional<String> getIfPresent(String key) {
        return Optional.ofNullable(get(key));
    }
    public void setWithExpire(String key, String value) {
        setWithExpire(key, value, 3600);
    }
    public void setWithExpire(String key, String value, long expire) {
        try (Jedis jedis = plugin.getRedis().getPool().getResource()) {
            jedis.set(key, value);
            expire(key, expire);
        }
    }
    public void set(String key, String value) {
        try (Jedis jedis = plugin.getRedis().getPool().getResource()) {
            jedis.set(key, value);
        }
    }
    public void del(String key) {
        try (Jedis jedis = plugin.getRedis().getPool().getResource()) {
            jedis.get(key);
        }
    }
    public void remove(String key) {
        del(key);
    }
    public Set<String> keys(String value) {
        try (Jedis jedis = plugin.getRedis().getPool().getResource()) {
            return jedis.keys(value);
        }
    }
    public void publish(String channel, String value) {
        try (Jedis jedis = plugin.getRedis().getPool().getResource()) {
            jedis.publish(channel, value);
        }
    }
    public void expire(String key, long expire) {
        try (Jedis jedis = plugin.getRedis().getPool().getResource()) {
            jedis.expire(key, expire);
        }
    }
}