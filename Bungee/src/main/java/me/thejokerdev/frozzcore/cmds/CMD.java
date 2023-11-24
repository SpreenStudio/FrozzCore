package me.thejokerdev.frozzcore.cmds;

import me.thejokerdev.frozzcore.BungeeMain;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.List;

public abstract class CMD extends Command implements TabExecutor {
    public BungeeMain plugin;

    public CMD(BungeeMain plugin, String name, String... aliases){
        super(name, "", aliases);
        this.plugin = plugin;
    }

    public CMD(BungeeMain plugin, Configuration file){
        super(file.getString("name"), file.getString("permission"), file.getStringList("aliases").toArray(new String[0]));
        this.plugin = plugin;
    }

    public boolean check(CommandSender sender, String perm){
        if (!sender.hasPermission(perm)){
            plugin.getUtils().sendMessage(sender, "general.noPermissions");
        }
        return sender.hasPermission(perm);
    }

    public boolean justCheck(CommandSender sender, String perm){
        return sender.hasPermission(perm);
    }

    public abstract String name();

    public List<String> aliases() {
        return new ArrayList<>();
    }

    public abstract String permission();
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getPermission() {
        return permission();
    }

    @Override
    public abstract void execute(CommandSender sender, String[] args);

    public List<String> getListOf(Object... objects) {
        List<String> list = new ArrayList<>();
        for (Object object : objects) {
            list.add(object.toString());
        }
        return list;
    }
}
