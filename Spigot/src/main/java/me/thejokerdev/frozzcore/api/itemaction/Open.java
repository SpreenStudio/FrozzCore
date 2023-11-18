package me.thejokerdev.frozzcore.api.itemaction;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.type.ItemActionExecutor;
import me.thejokerdev.frozzcore.type.Menu;
import org.bukkit.entity.Player;

public class Open implements ItemActionExecutor {

    private static final SpigotMain plugin = SpigotMain.getPlugin();

    @Override
    public boolean onCommand(Player player, String label) {
        String s = label.replace("[open]", "");
        for (Menu menu : plugin.getClassManager().getMenusManager().getPlayerMenus(player).values()) {
            plugin.debug(menu.getMenuId() + " is loaded for " + player.getName());
        }
        Menu menu = plugin.getClassManager().getMenusManager().getPlayerMenu(player, s);
        if (menu == null) {
            plugin.getClassManager().getMenusManager().loadMenus(player);
            plugin.getUtils().sendMessage(player, "menus.not-exist");
            return true;
        }
        player.openInventory(menu.getInventory());
        return false;
    }
}
