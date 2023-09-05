package me.thejokerdev.frozzcore.api.events.party;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerCreatePartyEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    private final String player;

    public PlayerCreatePartyEvent(String var1) {
        player = var1;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
