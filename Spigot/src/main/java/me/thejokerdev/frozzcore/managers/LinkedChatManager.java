package me.thejokerdev.frozzcore.managers;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.api.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import studio.spreen.cloud.api.CloudAPI;

import java.util.ArrayList;
import java.util.List;

public class LinkedChatManager {

    SpigotMain plugin;
    private JedisPubSub jedisPubSub;
    private JedisPool pool;
    private FileUtils file;

    public LinkedChatManager(SpigotMain plugin){
        this.plugin = plugin;
    }

    public void init(){
        plugin.getLogger().severe("init linkedchat");
        file = plugin.getClassManager().getUtils().getFile("linkedchat.yml");
    }

    public void sendMessage(Player player, String format, String message){
        List<String> groupList = file.getStringList("to-send-groups", new ArrayList<>());
        plugin.getLogger().severe("try send to: "+groupList+" "+(pool != null));

        if(groupList.isEmpty() || pool == null)
            return;

        JSONArray groupListArray = new JSONArray(groupList);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("from", getServerName());
        jsonObject.put("player", player.getName());
        jsonObject.put("format", format);
        jsonObject.put("message", message);
        jsonObject.put("server-groups", groupListArray);

        try (Jedis jedis = pool.getResource()) {
            jedis.publish("linked-chat", jsonObject.toString());
        }
        plugin.getLogger().info("sended: linked-chat "+ jsonObject);
    }

    public void connect(JedisPool pool){
        this.pool = pool;
        jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                JSONObject jsonMessage = new JSONObject(message);
                plugin.getLogger().info("received: "+channel+" "+ jsonMessage);
                if(jsonMessage == null || !channel.equalsIgnoreCase("linked-chat"))
                    return;

                String from = jsonMessage.getString("from-server");
                //String playerName = jsonMessage.getString("player");
                //String format = jsonMessage.getString("format");
                String playerMessage = jsonMessage.getString("message");
                JSONArray serverGroupsArray = jsonMessage.getJSONArray("server-groups");
                List<String> serverGroupsList = new ArrayList<>();

                for(int i = 0; i < serverGroupsArray.length(); i++)
                    serverGroupsList.add(serverGroupsArray.get(i).toString());

                if(from.equalsIgnoreCase(getServerName()) || !serverGroupsList.contains(getServerGroupname()))
                    return;

                for(Player player : Bukkit.getOnlinePlayers())
                    player.sendMessage(playerMessage);
            }
        };
        pool.getResource().subscribe(jedisPubSub);
    }

    private String getServerName(){
        return Bukkit.getServerName();
    }

    private String getServerGroupname(){
        return CloudAPI.getUniversalAPI().getServer(getServerName()).getGroup().getName();
    }

    public void disconnect(){
        if(jedisPubSub != null)
            jedisPubSub.unsubscribe();
    }
}
