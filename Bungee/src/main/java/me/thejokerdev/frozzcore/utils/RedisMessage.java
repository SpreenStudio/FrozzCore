package me.thejokerdev.frozzcore.utils;

import com.google.gson.Gson;
import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.redis.payload.Payload;

import java.util.HashMap;
import java.util.Map;

public class RedisMessage {
    private final String proxy;
    private final Payload payload;
    private final Map<String, String> params;

    public RedisMessage(BungeeMain plugin, Payload payload) {
        proxy = plugin.getProxyName();
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

    public String getProxy() {
        return this.proxy;
    }

    public Payload getPayload() {
        return this.payload;
    }
}
