package me.thejokerdev.frozzcore.api.events;

import lombok.Getter;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.redis.Redis;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class RedisInitEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    private SpigotMain plugin;
    private Redis redis;

    public RedisInitEvent(SpigotMain plugin, Redis redis){
        this.plugin = plugin;
        this.redis = redis;
    }
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
