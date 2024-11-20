package me.thejokerdev.frozzcore.commands.other.warp;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.type.CustomCMD;
import me.thejokerdev.frozzcore.type.Warp;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class DelWarpCMD extends CustomCMD {
    public DelWarpCMD(SpigotMain plugin) {
        super(plugin);

        setName("delwarp");
        setPermission("frozzcore.delwarp");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            getPlugin().getUtils().sendMessage(sender, "commands.delwarp.help");
            return false;
        }

        String warpID = args[0];
        Warp warp = getPlugin().getClassManager().getWarpManager().getWarp(warpID);

        if (warp == null) {
            getPlugin().getUtils().sendMessage(sender, "commands.warp.notFound");
            return false;
        }

        warp.delete();

        String msg = getPlugin().getUtils().getLangMSG(sender, "commands.delwarp.success");
        getPlugin().getUtils().sendMessage(sender, msg.replace("{warp}", warpID));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender.hasPermission(getPermission())) {
            List<String> list = new ArrayList<>();
            getPlugin().getClassManager().getWarpManager().getWarps().forEach(warp -> list.add(warp.getName()));
            return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
        }
        return new ArrayList<>();
    }
}
