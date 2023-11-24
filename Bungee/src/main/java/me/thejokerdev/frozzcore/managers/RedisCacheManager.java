package me.thejokerdev.frozzcore.managers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.calls.Callback;
import me.thejokerdev.frozzcore.redis.payload.Payload;
import me.thejokerdev.frozzcore.utils.RedisMessage;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RedisCacheManager implements Listener {
    private final BungeeMain plugin;
    private static RedisCacheManager instance;
    private final Cache<UUID, String> proxyCache = createCache();
    private final Cache<UUID, String> serverCache = createCache();
    private final Cache<UUID, String> playerCache = createCache();
    private final Cache<UUID, String> ipCache = createCache();
    private final Map<String, Callback<String>> callbacks;

    public RedisCacheManager(BungeeMain plugin) {
        this.plugin = plugin;
        instance = this;
        this.callbacks = new HashMap();
    }

    private static <K, V> Cache<K, V> createCache() {
        return CacheBuilder.newBuilder().maximumSize(1000L).expireAfterWrite(1L, TimeUnit.HOURS).build();
    }

    public static RedisCacheManager get(BungeeMain plugin) {
        return instance == null ? new RedisCacheManager(plugin) : instance;
    }

    public String getServer(UUID uuid) {
        ProxiedPlayer player = this.plugin.getProxy().getPlayer(uuid);
        if (player != null) {
            return player.getServer() != null ? player.getServer().getInfo().getName() : null;
        } else {
            try {
                return this.serverCache.get(uuid, () -> {
                    Jedis pool = this.plugin.getRedis().getPool().getResource();
                    Throwable var3 = null;

                    String var4;
                    try {
                        var4 = Objects.requireNonNull(pool.hget("player:data:" + uuid, "server"), "not found");
                    } catch (Throwable var13) {
                        var3 = var13;
                        throw var13;
                    } finally {
                        if (pool != null) {
                            if (var3 != null) {
                                try {
                                    pool.close();
                                } catch (Throwable var12) {
                                    var3.addSuppressed(var12);
                                }
                            } else {
                                pool.close();
                            }
                        }

                    }

                    return var4;
                });
            } catch (ExecutionException var4) {
                if (var4.getCause() instanceof NullPointerException && var4.getCause().getMessage().equals("not found")) {
                    return null;
                } else {
                    throw new RuntimeException("Unable to get server for " + uuid, var4);
                }
            }
        }
    }

    public String getProxy(UUID uuid) {
        ProxiedPlayer player = this.plugin.getProxy().getPlayer(uuid);
        if (player != null) {
            return plugin.getProxyName();
        } else {
            try {
                return this.proxyCache.get(uuid, () -> {
                    Jedis pool = this.plugin.getRedis().getPool().getResource();
                    Throwable var3 = null;

                    String var4;
                    try {
                        var4 = Objects.requireNonNull(pool.hget("player:data:" + uuid, "proxy"), "not found");
                    } catch (Throwable var13) {
                        var3 = var13;
                        throw var13;
                    } finally {
                        if (pool != null) {
                            if (var3 != null) {
                                try {
                                    pool.close();
                                } catch (Throwable var12) {
                                    var3.addSuppressed(var12);
                                }
                            } else {
                                pool.close();
                            }
                        }

                    }

                    return var4;
                });
            } catch (ExecutionException var4) {
                if (var4.getCause() instanceof NullPointerException && var4.getCause().getMessage().equals("not found")) {
                    return null;
                } else {
                    throw new RuntimeException("Unable to get proxy for " + uuid, var4);
                }
            }
        }
    }

    public String getIP(UUID uuid) {
        ProxiedPlayer player = this.plugin.getProxy().getPlayer(uuid);
        if (player != null) {
            return player.getPendingConnection().getSocketAddress().toString().split("/")[1].split(":")[0];
        } else {
            try {
                return this.ipCache.get(uuid, () -> {
                    Jedis pool = this.plugin.getRedis().getPool().getResource();
                    Throwable var3 = null;

                    String var4;
                    try {
                        var4 = pool.hget("player:data:" + uuid, "ip");
                    } catch (Throwable var13) {
                        var3 = var13;
                        throw var13;
                    } finally {
                        if (pool != null) {
                            if (var3 != null) {
                                try {
                                    pool.close();
                                } catch (Throwable var12) {
                                    var3.addSuppressed(var12);
                                }
                            } else {
                                pool.close();
                            }
                        }

                    }

                    return var4;
                });
            } catch (ExecutionException var4) {
                if (var4.getCause() instanceof NullPointerException && var4.getCause().getMessage().equals("not found")) {
                    return null;
                } else {
                    throw new RuntimeException("Unable to get ip for " + uuid, var4);
                }
            }
        }
    }

    public String getName(UUID uuid) {
        ProxiedPlayer player = this.plugin.getProxy().getPlayer(uuid);
        if (player != null) {
            return player.getName();
        } else {
            Jedis pool = this.plugin.getRedis().getPool().getResource();
            Throwable var5 = null;

            Object var7;
            try {
                String result = pool.hget("player:data:" + uuid, "name");
                if (result != null) {
                    String name = result;
                    return name;
                }

                var7 = null;
            } catch (Throwable var17) {
                var5 = var17;
                throw var17;
            } finally {
                if (pool != null) {
                    if (var5 != null) {
                        try {
                            pool.close();
                        } catch (Throwable var16) {
                            var5.addSuppressed(var16);
                        }
                    } else {
                        pool.close();
                    }
                }

            }

            return (String)var7;
        }
    }

    public UUID getUUIDFromName(String name) {
        ProxiedPlayer player = this.plugin.getProxy().getPlayer(name);
        if (player != null) {
            return player.getUniqueId();
        } else {
            try {
                Jedis pool = this.plugin.getRedis().getPool().getResource();
                Throwable var5 = null;

                UUID uuid;
                try {
                    String result = pool.hget("player:name:" + name.toLowerCase(), "uuid");
                    if (result == null) {
                        Object var7 = null;
                        return (UUID)var7;
                    }

                    uuid = UUID.fromString(result);
                } catch (Throwable var18) {
                    var5 = var18;
                    throw var18;
                } finally {
                    if (pool != null) {
                        if (var5 != null) {
                            try {
                                pool.close();
                            } catch (Throwable var17) {
                                var5.addSuppressed(var17);
                            }
                        } else {
                            pool.close();
                        }
                    }

                }

                return uuid;
            } catch (NullPointerException var20) {
                return null;
            }
        }
    }

    public boolean isOnline(UUID uuid) {
        ProxiedPlayer player = this.plugin.getProxy().getPlayer(uuid);
        if (player != null) {
            return true;
        } else if (this.playerCache.asMap().containsKey(uuid)) {
            return true;
        } else {
            Jedis pool = this.plugin.getRedis().getPool().getResource();
            Throwable var4 = null;

            boolean var6;
            try {
                String result = pool.hget("player:data:" + uuid, "name");
                var6 = result != null;
            } catch (Throwable var15) {
                var4 = var15;
                throw var15;
            } finally {
                if (pool != null) {
                    if (var4 != null) {
                        try {
                            pool.close();
                        } catch (Throwable var14) {
                            var4.addSuppressed(var14);
                        }
                    } else {
                        pool.close();
                    }
                }

            }

            return var6;
        }
    }

    public int getPlayersCount() {
        Jedis jedis = this.plugin.getRedis().getPool().getResource();
        Throwable var2 = null;

        int var4;
        try {
            Set<String> keys = jedis.keys("player:data:*");
            var4 = keys.size();
        } catch (Throwable var13) {
            var2 = var13;
            throw var13;
        } finally {
            if (jedis != null) {
                if (var2 != null) {
                    try {
                        jedis.close();
                    } catch (Throwable var12) {
                        var2.addSuppressed(var12);
                    }
                } else {
                    jedis.close();
                }
            }

        }

        return var4;
    }

    public Set<String> getPlayers(String filter) {
        Jedis jedis = this.plugin.getRedis().getPool().getResource();
        Throwable var3 = null;

        try {
            Set<String> keys = jedis.keys("player:data:*");
            Set<String> players = new HashSet();
            Iterator var6 = keys.iterator();

            while(var6.hasNext()) {
                String key = (String)var6.next();
                String name = jedis.hget(key, "name");
                if (name != null && (filter == null || name.toLowerCase().startsWith(filter.toLowerCase()))) {
                    players.add(name);
                }
            }

            Set<String> var18 = players;
            return var18;
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
    }

    public void createPlayer(ProxiedPlayer player, Pipeline pipeline) {
        Map<String, String> data = new HashMap<>(4);
        data.put("ip", player.getPendingConnection().getSocketAddress().toString().split("/")[1].split(":")[0]);
        data.put("proxy", plugin.getProxyName());
        data.put("name", player.getName());
        data.put("server", "");
        pipeline.sadd("proxy:users:" + plugin.getProxyName(), player.getUniqueId().toString());
        pipeline.hmset("player:data:" + player.getUniqueId(), data);
        pipeline.hset("player:name:" + player.getName().toLowerCase(), "uuid", player.getUniqueId().toString());
    }

    public void removePlayer(String player, String name, Jedis j) {
        j.srem("proxy:users:" + plugin.getProxyName(), player);
        j.hdel("player:data:" + player, "server", "ip", "proxy", "name");
        j.hdel("player:name:" + name.toLowerCase(), "uuid");
        this.plugin.getRedis().write((new RedisMessage(plugin, Payload.LEAVE)).setParam("uuid", player).setParam("proxy", plugin.getProxyName()).toJSON());
    }

    public void removePlayer(String player, String name, Pipeline j) {
        j.srem("proxy:users:" + plugin.getProxyName(), player);
        j.hdel("player:data:" + player, "server", "ip", "proxy", "name");
        j.hdel("player:name:" + name.toLowerCase(), "uuid");
        this.plugin.getRedis().write((new RedisMessage(plugin, Payload.LEAVE)).setParam("uuid", player).setParam("proxy", plugin.getProxyName()).toJSON());
    }

    public void removeData(UUID uuid) {
        this.ipCache.invalidate(uuid);
        this.playerCache.invalidate(uuid);
        this.serverCache.invalidate(uuid);
        this.proxyCache.invalidate(uuid);
    }

    public void sendMessage(UUID uuid, String message) {
        ProxiedPlayer player = this.plugin.getProxy().getPlayer(uuid);
        if (player != null) {
            player.sendMessage(TextComponent.fromLegacyText(message));
        } else {
            String msg = (new RedisMessage(plugin, Payload.MESSAGE)).setParam("uuid", uuid.toString()).setParam("message", message).toJSON();
            this.plugin.getRedis().write(msg);
        }
    }

    public void sendMessage(UUID uuid, TextComponent message) {
        ProxiedPlayer player = this.plugin.getProxy().getPlayer(uuid);
        if (player != null) {
            player.sendMessage(message);
        } else {
            String msg = (new RedisMessage(plugin, Payload.MESSAGE)).setParam("uuid", uuid.toString()).setParam("message", (new Gson()).toJson(message, TextComponent.class)).toJSON();
            this.plugin.getRedis().write(msg);
        }
    }

    public void connectPlayer(UUID uuid, String server) {
        ProxiedPlayer player = this.plugin.getProxy().getPlayer(uuid);
        if (player != null) {
            player.connect(this.plugin.getProxy().getServerInfo(server));
        } else {
            String msg = (new RedisMessage(plugin, Payload.CONNECT_PLAYER)).setParam("uuid", uuid.toString()).setParam("server", server).toJSON();
            this.plugin.getRedis().write(msg);
        }
    }

    public void clearCacheFromRedis() {
        Jedis jedis = this.plugin.getRedis().getPool().getResource();
        Throwable var2 = null;
        try {
            Set<String> keys = jedis.keys("player:data:*");
            for (String key : keys)
                jedis.del(key);
        } catch (Throwable var16) {
            var2 = var16;
            throw var16;
        } finally {
            if (jedis != null)
                if (var2 != null) {
                    try {
                        jedis.close();
                    } catch (Throwable var15) {
                        var2.addSuppressed(var15);
                    }
                } else {
                    jedis.close();
                }
        }
    }

    public Cache<UUID, String> getProxyCache() {
        return this.proxyCache;
    }

    public Cache<UUID, String> getServerCache() {
        return this.serverCache;
    }

    public Cache<UUID, String> getPlayerCache() {
        return this.playerCache;
    }

    public Cache<UUID, String> getIpCache() {
        return this.ipCache;
    }

    public Map<String, Callback<String>> getCallbacks() {
        return this.callbacks;
    }
}
