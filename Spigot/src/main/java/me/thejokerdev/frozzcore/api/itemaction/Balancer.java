package me.thejokerdev.frozzcore.api.itemaction;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.type.ItemActionExecutor;
import org.bukkit.entity.Player;

public class Balancer implements ItemActionExecutor {

    @Override
    public boolean onCommand(Player player, String label) {
        SpigotMain.getPlugin().getPluginMessageManager().connectPlayer(player, label.split("]")[1]);
        return false;
    }
}
