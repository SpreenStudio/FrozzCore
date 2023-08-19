package me.thejokerdev.frozzcore.api.events;

import lombok.Getter;
import me.thejokerdev.frozzcore.api.data.NameTag;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.beans.ConstructorProperties;

@Getter
public class NametagFirstLoadedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final NameTag nametag;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @ConstructorProperties({"player", "nametag"})
    public NametagFirstLoadedEvent(Player player, NameTag nametag) {
        this.player = player;
        this.nametag = nametag;
    }
}
