package me.thejokerdev.frozzcore.managers;

import me.thejokerdev.frozzcore.BungeeMain;

import java.util.HashMap;
import java.util.Map;

public class Placeholders {
    private final BungeeMain plugin;
    private HashMap<String, String> placeholders;

    public Placeholders(BungeeMain plugin) {
        this.plugin = plugin;
    }

    public void init(){
        placeholders = new HashMap<>();

        for (String s : plugin.getConfig().getStringList("placeholders")){
            String[] args = s.split("\\|");
            placeholders.put(args[0], args[1]);
        }
    }

    public String apply(String msg){
        if (!placeholders.isEmpty()){
            for (Map.Entry<String, String> map : placeholders.entrySet()){
                msg = msg.replace(map.getKey(), map.getValue());
            }
        }
        return msg;
    }
}
