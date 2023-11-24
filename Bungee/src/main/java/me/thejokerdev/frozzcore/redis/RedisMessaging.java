package me.thejokerdev.frozzcore.redis;

import me.thejokerdev.frozzcore.BungeeMain;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RedisMessaging {
  private final BungeeMain plugin;
  
  private final ExecutorService executorService;
  
  public RedisMessaging(BungeeMain plugin) {
    this.plugin = plugin;
    this.executorService = Executors.newCachedThreadPool();
  }
  
  public void subscribe(String channel, final Consumer<String> consumer) {
      this.executorService.submit(() -> {
          try {
              Jedis jedis = this.plugin.getRedis().getPool().getResource();
              try {
                  jedis.subscribe(new JedisPubSub() {
                      public void onMessage(String channel, String message) {
                          consumer.accept(message);
                      }
                  }, channel);
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
          } catch (Exception exception) {
              exception.printStackTrace();
          }
      });
  }
}
