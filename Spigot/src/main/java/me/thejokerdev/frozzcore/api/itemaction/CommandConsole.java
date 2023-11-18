package me.thejokerdev.frozzcore.api.itemaction;

import me.thejokerdev.frozzcore.type.ItemActionExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandConsole implements ItemActionExecutor {

    @Override
    public boolean onCommand(Player player, String label) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), label.split("]")[1]);
        return false;
    }
}
