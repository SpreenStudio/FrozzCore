package me.thejokerdev.frozzcore.api.itemaction;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.type.ItemActionExecutor;
import org.bukkit.entity.Player;

public class Sound implements ItemActionExecutor {

    @Override
    public boolean onCommand(Player player, String label) {
        SpigotMain.getPlugin().getUtils().playAudio(player, label.split("]")[1]);
        return false;
    }
}
