package me.thejokerdev.frozzcore.api.itemaction;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.type.ItemActionExecutor;
import org.bukkit.entity.Player;

public class Close implements ItemActionExecutor {

    @Override
    public boolean onCommand(Player player, String label) {
        player.closeInventory();
        return false;
    }
}
