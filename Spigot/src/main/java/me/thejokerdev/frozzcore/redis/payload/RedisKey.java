package me.thejokerdev.frozzcore.redis.payload;

public enum RedisKey {
    SERVER_ADD(3600),
    SERVER_REMOVE(3600),
    LINKED_CHAT(3600);

    private long expire;
    RedisKey(long expire){
        this.expire = expire;
    }

    public String getID(){
        return this.name().toLowerCase();
    }

    public long getExpire() {
        return expire;
    }
}
