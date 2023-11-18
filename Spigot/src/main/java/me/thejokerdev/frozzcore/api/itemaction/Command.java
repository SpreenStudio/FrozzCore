package me.thejokerdev.frozzcore.api.itemaction;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.type.ItemActionExecutor;
import org.bukkit.entity.Player;

public class Command implements ItemActionExecutor {

    @Override
    public boolean onCommand(Player player, String label) {
        player.chat("/"+label.split("]")[1]);
        return false;
    }
}
