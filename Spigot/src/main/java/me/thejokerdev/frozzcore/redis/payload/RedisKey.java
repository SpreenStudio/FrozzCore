package me.thejokerdev.frozzcore.redis.payload;

import lombok.Getter;

@Getter
public enum RedisKey {
    SERVER_ADD(3600),
    SERVER_REMOVE(3600),
    LINKED_CHAT(3600);

    private final long expire;
    RedisKey(long expire){
        this.expire = expire;
    }

    public String getID(){
        return this.name().toLowerCase();
    }

}
