package me.thejokerdev.frozzcore.api.itemaction;

import me.thejokerdev.frozzcore.type.ItemActionExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandOp implements ItemActionExecutor {

    @Override
    public boolean onCommand(Player player, String label) {
        Bukkit.dispatchCommand(player, label.split("]")[1]);
        return false;
    }
}
