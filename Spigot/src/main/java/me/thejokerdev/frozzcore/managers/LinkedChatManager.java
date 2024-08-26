package me.thejokerdev.frozzcore.managers;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.api.utils.FileUtils;
import me.thejokerdev.frozzcore.redis.payload.RedisKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONArray;
import org.json.JSONObject;
import studio.spreen.cloud.api.CloudAPI;

import java.util.ArrayList;
import java.util.List;

public class LinkedChatManager {

    SpigotMain plugin;
    private FileUtils file;

    public LinkedChatManager(SpigotMain plugin){
        this.plugin = plugin;
    }

    public void init(){
        registerChannel();
        file = plugin.getClassManager().getUtils().getFile("linkedchat.yml");
    }

    public void sendMessage(Player player, String format, String message){
        new BukkitRunnable(){
            @Override
            public void run() {
                List<String> groupList = file.getStringList("to-send-groups", new ArrayList<>());

                if(groupList.isEmpty())
                    return;

                JSONArray groupListArray = new JSONArray(groupList);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("from", getServerName());
                jsonObject.put("player", player.getName());
                jsonObject.put("message", message);
                jsonObject.put("server-groups", groupListArray);

                //fix double %% in format
                jsonObject.put("format", format.replace("%%", "%"));

                plugin.getRedis().getRedisManager().setWithExpire(jsonObject.toString(), new JSONObject().toString(), RedisKey.LINKED_CHAT.getExpire());
                plugin.getRedis().getRedisManager().publish(RedisKey.LINKED_CHAT.getID(), jsonObject.toString());
                plugin.debug("LinkedChat message sent: " + jsonObject);
            }
        }.runTaskAsynchronously(plugin);
    }

    public void registerChannel(){
        plugin.getRedis().getRedisMessaging().subscribe(RedisKey.LINKED_CHAT.getID(), (message) ->
        {
            JSONObject jsonMessage = new JSONObject(message);
            String from = jsonMessage.getString("from");
            String playerName = jsonMessage.getString("player");
            String format = jsonMessage.getString("format");
            //String playerMessage = jsonMessage.getString("message");
            JSONArray serverGroupsArray = jsonMessage.getJSONArray("server-groups");
            List<String> serverGroupsList = new ArrayList<>();

            for(int i = 0; i < serverGroupsArray.length(); i++) {
                serverGroupsList.add(serverGroupsArray.get(i).toString());
            }

            plugin.debug("Received message: "+ jsonMessage);
            plugin.debug("This server group: "+getServerGroupname());
            plugin.debug("This server name: "+getServerName());

            if(plugin.getServer().getPlayer(playerName)!=null || !serverGroupsList.contains(getServerGroupname())) return;

            serverGroupsList.forEach(plugin::debug);

            if (serverGroupsList.contains(getServerGroupname())) {
                plugin.debug("Sent message");
                plugin.getServer().getConsoleSender().sendMessage(format);
                for(Player player : Bukkit.getOnlinePlayers())
                    player.sendMessage(format);
            }
        });
    }

    private String getServerName(){
        return Bukkit.getName();
    }

    private String getServerGroupname(){
        boolean existsCloud = plugin.getServer().getPluginManager().isPluginEnabled("Cloud");
        return existsCloud ? CloudAPI.getUniversalAPI().getServer(getServerName()).getGroup().getName() : file.getString("group", "");
    }
}
