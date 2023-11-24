package me.thejokerdev.frozzcore.redis.listener;

import com.google.gson.Gson;
import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.managers.RedisCacheManager;
import me.thejokerdev.frozzcore.utils.RedisMessage;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RedisListener extends JedisPubSub {
    private final BungeeMain plugin;
    public RedisListener(BungeeMain plugin) {
        this.plugin = plugin;
    }

    public void onMessage(String channel, String message) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            RedisMessage redisMessage = (new Gson()).fromJson(message, RedisMessage.class);
            RedisCacheManager cacheManager = RedisCacheManager.get(plugin);
            ProxiedPlayer p;
            UUID uuid;
            switch (redisMessage.getPayload()) {
                case JOIN :{
                    uuid = UUID.fromString(redisMessage.getParam("uuid"));
                    cacheManager.getProxyCache().put(uuid, redisMessage.getParam("proxy"));
                    cacheManager.getIpCache().put(uuid, redisMessage.getParam("ip"));
                    cacheManager.getPlayerCache().put(uuid, redisMessage.getParam("name").toLowerCase());
                    break;
                }
                case LEAVE : cacheManager.removeData(UUID.fromString(redisMessage.getParam("uuid"))); break;
                case SERVER_CHANGE :{
                    uuid = UUID.fromString(redisMessage.getParam("uuid"));
                    cacheManager.getServerCache().put(uuid, redisMessage.getParam("server"));
                    break;
                }
                case MESSAGE:
                case MESSAGE_COMPONENT : {
                    p = plugin.getProxy().getPlayer(UUID.fromString(redisMessage.getParam("uuid")));
                    if (p != null && p.isConnected()) {
                        plugin.getUtils().sendMessage(p, redisMessage.getParam("message"));
                    }
                    break;
                }
                case CONNECT_PLAYER : {
                    p = plugin.getProxy().getPlayer(UUID.fromString(redisMessage.getParam("uuid")));
                    if (p != null && p.isConnected()) {
                        ServerInfo serverInfo = plugin.getProxy().getServerInfo(redisMessage.getParam("server"));
                        if (serverInfo == null) {
                            return;
                        }

                        p.connect(serverInfo);
                    }
                    break;
                }
                case SERVER_ADD : {
                    String name = redisMessage.getParam("name");
                    String ip = redisMessage.getParam("ip");
                    String port = redisMessage.getParam("port");
                    {
                        String server = name+","+ip+","+port;
                        List<String> list = new ArrayList<>(plugin.getFileUtils().getServersCache().getStringList("servers"));
                        list.add(server);
                        plugin.getFileUtils().getServersCache().set("servers", list);
                        plugin.getFileUtils().saveServersCache();
                    }
                    InetSocketAddress address = new InetSocketAddress(ip, Integer.parseInt(port));
                    plugin.getProxy().getServers().put(name, plugin.getProxy().constructServerInfo(name, address, "", false));
                    String info = "&fServer name: &b"+name+" &7| &fServer IP: &e"+ip+" &7| &fServer Port: &e"+port;
                    plugin.log("{prefix}&aAdded server to proxy -> " + info);
                    break;
                }
                case SERVER_REMOVE :{
                    String name = redisMessage.getParam("name");
                    ServerInfo serverInfo = plugin.getProxy().getServerInfo(name);
                    if (serverInfo == null){
                        plugin.log("{prefix}&cAttempted to remove server from proxy but it doesn't exist -> &fServer name: &b"+name);
                        return;
                    }
                    String ip = serverInfo.getAddress().getHostString();
                    String port = String.valueOf(serverInfo.getAddress().getPort());
                    plugin.getProxy().getServers().remove(name);
                    {
                        String server = name+","+ip+","+port;
                        List<String> list = new ArrayList<>(plugin.getFileUtils().getServersCache().getStringList("servers"));
                        list.remove(server);
                        plugin.getFileUtils().getServersCache().set("servers", list);
                        plugin.getFileUtils().saveServersCache();
                    }
                    String info = "&fServer name: &b"+name+" &7| &fServer IP: &e"+ip+" &7| &fServer Port: &e"+port;
                    plugin.log("{prefix}&cRemoved server of proxy -> " + info);
                    break;
                }
                default : plugin.getLogger().info("[Redis] The message was received, but there was no response"); break;
            }
        });
    }
}
