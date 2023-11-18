package me.thejokerdev.frozzcore.api.itemaction;

import com.cryptomorin.xseries.messages.Titles;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.enums.ModifierStatus;
import me.thejokerdev.frozzcore.type.FUser;
import me.thejokerdev.frozzcore.type.ItemActionExecutor;
import org.bukkit.entity.Player;

public class Speed implements ItemActionExecutor {

    private static final SpigotMain plugin = SpigotMain.getPlugin();

    @Override
    public boolean onCommand(Player player, String label) {
        FUser user = plugin.getClassManager().getPlayerManager().getUser(player);
        user.setSpeed(user.getSpeed() == ModifierStatus.OFF ? ModifierStatus.ON : ModifierStatus.OFF);
        user.saveData(false);
        return false;
    }
}
