package me.thejokerdev.frozzcore.cmds.custom;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.cmds.CMD;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.List;

public class CustomCMD extends CMD {
    private final String name;
    private final List<String> aliases;
    private final String permission;
    private final List<String> actions;

    public CustomCMD(BungeeMain plugin, Configuration file){
        super(plugin, file);

        name = file.getString("name");
        aliases = new ArrayList<>();
        aliases.addAll(file.getStringList("aliases"));

        permission = file.getString("permission");

        actions = new ArrayList<>();
        actions.addAll(file.getStringList("actions"));
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<String> aliases() {
        if (!aliases.isEmpty()){
            return aliases;
        }
        return new ArrayList<>();
    }

    @Override
    public String permission() {
        if (permission == null){
            return null;
        }
        if (permission.equalsIgnoreCase("none") || permission.equalsIgnoreCase("null") || permission.equalsIgnoreCase("")){
            return "";
        }
        return permission;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.getUtils().executeActions(sender, actions);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
