package me.thejokerdev.frozzcore.redis;

import me.thejokerdev.frozzcore.BungeeMain;
import redis.clients.jedis.Jedis;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RedisManager {
    private final BungeeMain plugin;

    public RedisManager(BungeeMain plugin) {
        this.plugin = plugin;
    }

    public void getIfPresent(String key, Consumer<String> consumer) {
        (new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, 5000, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(50000))).submit(() -> {
            Jedis jedis = plugin.getRedis().getPool().getResource();
            try {
                consumer.accept(jedis.get(key));
                if (jedis != null)
                    jedis.close();
            } catch (Throwable throwable) {
                if (jedis != null)
                    try {
                        jedis.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        });
    }

    public String get(String key) {
        Jedis jedis = plugin.getRedis().getPool().getResource();
        try {
            String str = jedis.get(key);
            if (jedis != null)
                jedis.close();
            return str;
        } catch (Throwable throwable) {
            if (jedis != null)
                try {
                    jedis.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
    }

    public String getDel(String key) {
        Jedis jedis = plugin.getRedis().getPool().getResource();
        try {
            String str = jedis.getDel(key);
            if (jedis != null)
                jedis.close();
            return str;
        } catch (Throwable throwable) {
            if (jedis != null)
                try {
                    jedis.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
    }

    public boolean hasDel(String key) {
        return Optional.ofNullable(getDel(key)).isPresent();
    }

    public boolean has(String key) {
        return (get(key) != null);
    }

    public Optional<String> getIfPresent(String key) {
        return Optional.ofNullable(get(key));
    }

    public void setWithExpire(String key, String value) {
        setWithExpire(key, value, 3600L);
    }

    public void setWithExpire(String key, String value, long expire) {
        Jedis jedis = plugin.getRedis().getPool().getResource();
        try {
            jedis.set(key, value);
            expire(key, expire);
            if (jedis != null)
                jedis.close();
        } catch (Throwable throwable) {
            if (jedis != null)
                try {
                    jedis.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
    }

    public void set(String key, String value) {
        Jedis jedis = plugin.getRedis().getPool().getResource();
        try {
            jedis.set(key, value);
            if (jedis != null)
                jedis.close();
        } catch (Throwable throwable) {
            if (jedis != null)
                try {
                    jedis.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
    }

    public void del(String key) {
        Jedis jedis = plugin.getRedis().getPool().getResource();
        try {
            jedis.get(key);
            jedis.close();
        } catch (Throwable throwable) {
            if (jedis != null)
                try {
                    jedis.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
    }

    public void remove(String key) {
        del(key);
    }

    public Set<String> keys(String value) {
        Jedis jedis = plugin.getRedis().getPool().getResource();
        try {
            Set<String> set = jedis.keys(value);
            jedis.close();
            return set;
        } catch (Throwable throwable) {
            if (jedis != null)
                try {
                    jedis.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
    }

    public void publish(String channel, String value) {
        Jedis jedis = plugin.getRedis().getPool().getResource();
        try {
            jedis.publish(channel, value);
            jedis.close();
        } catch (Throwable throwable) {
            if (jedis != null)
                try {
                    jedis.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
    }

    public void expire(String key, long expire) {
        Jedis jedis = plugin.getRedis().getPool().getResource();
        try {
            jedis.expire(key, expire);
            jedis.close();
        } catch (Throwable throwable) {
            if (jedis != null)
                try {
                    jedis.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
    }
}
