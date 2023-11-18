package me.thejokerdev.frozzcore.api.itemaction;

import com.cryptomorin.xseries.messages.Titles;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.type.ItemActionExecutor;
import org.bukkit.entity.Player;

public class Title implements ItemActionExecutor {

    private static final SpigotMain plugin = SpigotMain.getPlugin();

    @Override
    public boolean onCommand(Player player, String label) {
        String s = label.replace("[title]", "");
        String[] split = s.split("`");
        if (split.length <= 2) {
            Titles.sendTitle(player, formatMSG(player, split[0]), formatMSG(player, split[1]));
        }
        if (split.length == 5) {
            Titles.sendTitle(player,
                    Integer.parseInt(split[2]),
                    Integer.parseInt(split[3]),
                    Integer.parseInt(split[4]),
                    formatMSG(player, split[0]),
                    formatMSG(player, split[1]));
        }
        return false;
    }

    public String formatMSG(Player player, String message) {
        return plugin.getUtils().formatMSG(player, message);
    }
}
