//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.thejokerdev.frozzcore.redis.payload;

import com.google.gson.Gson;
import lombok.Getter;

import java.util.UUID;

@Getter
public class PlayerData {
    private final UUID uuid;
    private final String name;
    private final String serverName;

    public PlayerData(UUID uuid, String name, String serverName) {
        this.uuid = uuid;
        this.name = name;
        this.serverName = serverName;
    }

    public String toJSON() {
        return (new Gson()).toJson(this);
    }

}
