package me.thejokerdev.frozzcore.menus.custom;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.type.CustomCMD;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class MenuCMD extends CustomCMD {
    private final CustomMenu menu;
    public MenuCMD(SpigotMain plugin, ConfigurationSection section, CustomMenu menu) {
        super(plugin);
        setName(section.getString("name", menu.getMenuId()));
        if (section.get("aliases")!=null){
            addAliases(section.getStringList("aliases").toArray(new String[0]));
        }
        this.menu = menu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            ((Player)sender).openInventory(menu.getInventory());
        }
        return true;
    }
}
