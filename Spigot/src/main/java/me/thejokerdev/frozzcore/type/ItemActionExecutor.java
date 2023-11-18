package me.thejokerdev.frozzcore.type;

import org.bukkit.entity.Player;

public interface ItemActionExecutor {

    boolean onCommand(Player player, String label);

}
