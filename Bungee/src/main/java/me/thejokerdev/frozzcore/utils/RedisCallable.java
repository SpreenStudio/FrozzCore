package me.thejokerdev.frozzcore.utils;

import me.thejokerdev.frozzcore.BungeeMain;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.concurrent.Callable;
import java.util.logging.Level;

public abstract class RedisCallable<T> implements Callable<T>, Runnable {
    private final BungeeMain plugin;

    public T call() {
        return this.run(false);
    }

    public void run() {
        this.call();
    }

    private T run(boolean retry) {
        try {
            if (this.plugin.getRedis().getPool() == null) {
                throw new RuntimeException("Redis pool is null");
            }
            Jedis jedis = this.plugin.getRedis().getPool().getResource();
            Throwable var3 = null;

            Object var4;
            try {
                var4 = this.call(jedis);
            } catch (Throwable var16) {
                var3 = var16;
                throw var16;
            } finally {
                if (jedis != null) {
                    if (var3 != null) {
                        try {
                            jedis.close();
                        } catch (Throwable var15) {
                            var3.addSuppressed(var15);
                        }
                    } else {
                        jedis.close();
                    }
                }

            }

            return (T) var4;
        } catch (JedisConnectionException var18) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to get connection", var18);
            if (!retry) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException var14) {
                    throw new RuntimeException("task failed", var14);
                }

                return this.run(true);
            } else {
                throw new RuntimeException("task failed");
            }
        }
    }

    protected abstract T call(Jedis var1);

    public RedisCallable(BungeeMain plugin) {
        this.plugin = plugin;
    }
}
