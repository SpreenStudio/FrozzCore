package me.thejokerdev.frozzcore.api.events.party;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@Getter
public class PlayerDisbandPartyEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    private final String player;
    private final List<String> members;

    public PlayerDisbandPartyEvent(String var1, String var2) {
        player = var1;
        members = Arrays.asList(var2.split(","));
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
