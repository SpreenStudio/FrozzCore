package me.thejokerdev.frozzcore.managers;

import lombok.Getter;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.data.MongoDB;
import me.thejokerdev.frozzcore.data.MySQL;
import me.thejokerdev.frozzcore.data.SQLite;
import me.thejokerdev.frozzcore.type.Data;

@Getter
public class DataManager {
    private final SpigotMain plugin;
    @Getter
    private Data data;

    public DataManager(SpigotMain plugin) {
        this.plugin = plugin;

        initData();
    }

    public void initData(){
        String str = plugin.getConfig().getString("data.type", "yml");
        switch (str.toLowerCase()){
            case "mysql":{
                data = new MySQL(plugin);
                break;
            }
            case "mongodb": {
                data = new MongoDB(plugin);
                break;
            }
            case "sqlite":
            case "sql":
            default: {
                data = new SQLite(plugin);
                break;
            }
        }
        data.setup();
    }

}
