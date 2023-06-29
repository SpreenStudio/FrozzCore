package me.thejokerdev.frozzcore.api.events.party;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerCreatePartyEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    private final String player;

    public PlayerCreatePartyEvent(String var1) {
        player = var1;
    }

    public String getPlayer() {
        return player;
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
