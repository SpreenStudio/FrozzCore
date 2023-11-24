package me.thejokerdev.frozzcore.managers;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.utils.Platform;

import java.util.ArrayList;
import java.util.List;

public class PlatformManager {
    private final BungeeMain plugin;
    private static List<Platform> platforms;

    public PlatformManager(BungeeMain plugin) {
        this.plugin = plugin;
    }

    public void loadPlataforms(){
        platforms = new ArrayList<>();
        if (plugin.getConfig().get("stream.platforms")==null){
            return;
        }
        for (String s : plugin.getConfig().getSection("stream.platforms").getKeys()){
            platforms.add(new Platform(s, plugin.getConfig().getSection("stream.platforms."+s)));
        }
    }

    public boolean checkPlatform(String url){
        return getPlatform(url)!=null;
    }
    public Platform getPlatform(String url){
        if (platforms == null || platforms.isEmpty()){
            return null;
        }
        url = url.toLowerCase();
        for (Platform p : platforms){
            for (String s : p.getLinks()){
                if (url.contains(s)){
                    return p;
                }
            }
        }
        return null;
    }
}
