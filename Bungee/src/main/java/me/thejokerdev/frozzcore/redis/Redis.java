package me.thejokerdev.frozzcore.redis;

import lombok.Getter;
import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.redis.listener.RedisListener;
import net.md_5.bungee.config.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@Getter
public class Redis {
    private JedisPool pool;

    private PubSubHandler handler;

    private boolean active = false;

    private final BungeeMain plugin;

    private String database;

    private final RedisManager redisManager;

    private final RedisMessaging redisMessaging;

    public Redis(BungeeMain plugin) {
        this.plugin = plugin;
        redisManager = new RedisManager(plugin);
        redisMessaging = new RedisMessaging(plugin);
    }

    public void connect() {
        Configuration section = plugin.getConfig().getSection("redis");
        if (section.getString("auth.password").equals("uqqVbwm/XOY")) {
            plugin.log("{prefix}&cNo se ha configurado el archivo de configuración para redis.");
            return;
        }
        boolean hasPassword = !section.getString("auth.password", "").isEmpty();
        try {
            plugin.log("{prefix}&eConnecting to Redis...");
            FutureTask<JedisPool> task = new FutureTask<>(() -> {
                JedisPoolConfig config = new JedisPoolConfig();
                config.setMaxTotal(section.getInt("config.maxConnections", 8));
                if (hasPassword) {
                    config.setTestOnBorrow(true);
                    return new JedisPool(config, section.getString("host", "localhost"), section.getInt("port", 6379), 0, section.getString("auth.password", ""));
                }
                return new JedisPool(config, section.getString("host", "localhost"), section.getInt("port", 6379));
            });
            plugin.getProxy().getScheduler().runAsync(plugin, task);
            try {
                pool = task.get();
            } catch (ExecutionException | InterruptedException var4) {
                throw new RuntimeException("Unable to create Redis pool", var4);
            }
            handler = new PubSubHandler();
            plugin.getProxy().getScheduler().runAsync(plugin, handler);
            active = true;
            plugin.log("{prefix}&aSuccessfully connected to Redis");
        } catch (Exception var5) {
            plugin.log("{prefix}&cUnable to connect to Redis, disabling Redis.");
            active = false;
        }
        database = section.getString("database", "bcore_data");
    }

    public void disconnect() {
        pool.destroy();
        handler.clear();
    }

    public void write(String json) {
        write(database, json);
    }

    public void write(String channel, String json) {
        try {
            Jedis jedis = pool.getResource();
            Throwable var4 = null;
            try {
                jedis.publish(channel, json);
            } catch (Throwable var14) {
                var4 = var14;
                throw var14;
            } finally {
                if (jedis != null)
                    if (var4 != null) {
                        try {
                            jedis.close();
                        } catch (Throwable var13) {
                            var4.addSuppressed(var13);
                        }
                    } else {
                        jedis.close();
                    }
            }
        } catch (JedisConnectionException var16) {
            plugin.log("{prefix}&cUnable to get connection from pool - did your Redis server go away?");
            throw new RuntimeException("Unable to publish channel message", var16);
        }
    }

    class PubSubHandler implements Runnable {
        private RedisListener redisListener;

        private final Set<String> channels = new HashSet<>();

        public void run() {
            boolean retry = false;
            try {
                Jedis rsc = Redis.this.pool.getResource();
                Throwable var3 = null;
                try {
                    try {
                        redisListener = new RedisListener(Redis.this.plugin);
                        channels.add(Redis.this.database);
                        channels.add("bcore-" + Redis.this.plugin.getConfig().getString("proxyName"));
                        rsc.subscribe(redisListener, channels.toArray(new String[0]));
                    } catch (Exception var17) {
                        try {
                            redisListener.unsubscribe();
                        } catch (Exception ignored) {}
                        retry = true;
                    }
                } catch (Throwable var18) {
                    var3 = var18;
                    throw var18;
                } finally {
                    if (rsc != null)
                        if (var3 != null) {
                            try {
                                rsc.close();
                            } catch (Throwable var15) {
                                var3.addSuppressed(var15);
                            }
                        } else {
                            rsc.close();
                        }
                }
            } catch (JedisConnectionException var20) {
                plugin.log("{prefix}&cPubSub error, attempting to recover in 5 secs.");
                Redis.this.plugin.getProxy().getScheduler().schedule(Redis.this.plugin, this, 5L, TimeUnit.SECONDS);
            }
            if (retry)
                run();
        }

        public void addChannels(String... channel) {
            channels.addAll(Arrays.asList(channel));
            redisListener.subscribe(channel);
        }

        public void removeChannel(String... channel) {
            List<String> channels = Arrays.asList(channel);
            Objects.requireNonNull(channels);
            channels.forEach(channels::remove);
            redisListener.unsubscribe(channel);
        }

        public void clear() {
            channels.clear();
            if (redisListener != null)
                redisListener.unsubscribe();
        }
    }
}
