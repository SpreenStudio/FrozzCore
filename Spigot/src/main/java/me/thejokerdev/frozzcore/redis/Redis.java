package me.thejokerdev.frozzcore.redis;

import lombok.Getter;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.redis.payload.RedisKey;
import org.bukkit.configuration.ConfigurationSection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.function.Consumer;
import java.util.logging.Level;

public class Redis {
    @Getter
    private JedisPool pool;
    @Getter
    private boolean active = false;
    private final SpigotMain plugin;
    @Getter private final RedisManager redisManager;
    @Getter private final RedisMessaging redisMessaging;

    public Redis(SpigotMain plugin) {
        this.plugin = plugin;
        this.redisManager = new RedisManager(plugin);
        this.redisMessaging = new RedisMessaging(plugin);
    }

    private String database;

    public void connect() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("redis");

        try {
            plugin.console("{prefix}&eConectando a redis...");
            stablishConnection(section, pool -> {
                this.pool = pool;
                this.active = true;
                plugin.console("{prefix}&aConectado a redis.");
            });
        } catch (Exception var5) {
            plugin.console("{prefix}&cError al conectar a redis.");
            this.active = false;
        }

        database = section.getString("database", "bcore_data");
    }

    public void stablishConnection(ConfigurationSection section, final Consumer<JedisPool> consumer) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(2048);
        JedisPool pool;
        boolean hasPassword = !section.getString("auth.password", "").isEmpty();
        if (hasPassword) {
            config.setTestOnBorrow(true);
            pool = new JedisPool(config, section.getString("host", "localhost"), section.getInt("port", 6379), 30000, section.getString("auth.password", ""), 0);
        } else {
            pool = new JedisPool(config, section.getString("host", "localhost"), section.getInt("port", 6379), 30000);
        }
        consumer.accept(pool);
    }

    public void disconnect() {
        this.pool.destroy();
    }

    public void write(String json) {
        getRedisManager().publish(database, json);
    }

    public void addServer(String name, String ip, String port){
        String msg = (new RedisMessage(plugin, RedisKey.SERVER_ADD)).setParam("name", name).setParam("ip", ip).setParam("port", port).toJSON();
        write(msg);
    }

    public void removeServer(String name){
        String msg = (new RedisMessage(plugin, RedisKey.SERVER_REMOVE)).setParam("name", name).toJSON();
        write(msg);
    }
}
