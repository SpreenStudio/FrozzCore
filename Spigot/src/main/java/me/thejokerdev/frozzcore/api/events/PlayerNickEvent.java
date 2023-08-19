package me.thejokerdev.frozzcore.api.events;

import lombok.Getter;
import me.thejokerdev.frozzcore.type.FUser;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerNickEvent extends Event {
    @Getter
    private static final HandlerList handlerList = new HandlerList();
    private final FUser user;
    private final Cause cause;

    public PlayerNickEvent(FUser user, Cause cause){
        this.user = user;
        this.cause = cause;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public enum Cause {
        NICK,
        UNNICK
    }
}
