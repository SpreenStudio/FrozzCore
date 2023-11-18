package me.thejokerdev.frozzcore.api.itemaction;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.type.ItemActionExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Message implements ItemActionExecutor {

    @Override
    public boolean onCommand(Player player, String label) {
        String message = label.replace("[msg]", "");
        message = SpigotMain.getPlugin().getUtils().formatMSG(player, message);
        player.sendMessage(message);
        return false;
    }
}
