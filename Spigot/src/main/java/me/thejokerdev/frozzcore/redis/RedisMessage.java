package me.thejokerdev.frozzcore.redis;

import com.google.gson.Gson;
import lombok.Getter;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.redis.payload.RedisKey;

import java.util.HashMap;
import java.util.Map;

public class RedisMessage {
    @Getter
    private final RedisKey payload;
    private final Map<String, String> params;

    public RedisMessage(SpigotMain plugin, RedisKey payload) {
        this.payload = payload;
        this.params = new HashMap<>();
    }

    public RedisMessage setParam(String key, String value) {
        this.params.put(key, value);
        return this;
    }

    public String getParam(String key) {
        return this.containsParam(key) ? this.params.get(key) : null;
    }

    public boolean containsParam(String key) {
        return this.params.containsKey(key);
    }

    public void removeParam(String key) {
        if (this.containsParam(key)) {
            this.params.remove(key);
        }

    }

    public String toJSON() {
        return (new Gson()).toJson(this);
    }

}
