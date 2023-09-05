package me.thejokerdev.frozzcore.api.events;

import lombok.Getter;
import me.thejokerdev.frozzcore.enums.EconomyAction;
import me.thejokerdev.frozzcore.type.FUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class EconomyChangeEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    private final FUser user;
    private final EconomyAction action;
    private final double amount;

    public EconomyChangeEvent(FUser var1, EconomyAction var2, double var3){
        user = var1;
        action = var2;
        amount = var3;
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
