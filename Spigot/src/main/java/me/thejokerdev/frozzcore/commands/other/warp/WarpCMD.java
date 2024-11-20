package me.thejokerdev.frozzcore.commands.other.warp;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.type.CustomCMD;
import me.thejokerdev.frozzcore.type.Warp;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WarpCMD extends CustomCMD {
    public WarpCMD(SpigotMain plugin) {
        super(plugin);
        setPermission("frozzcore.warp");
        setName("warp");
        setAliases(Collections.singletonList("warps"));
        setTabComplete(true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            getPlugin().getUtils().sendMessage(sender, "noPermission");
            return false;
        }
        if (args.length == 0) {
            getPlugin().getUtils().sendMessage(sender, "commands.warp.help");
            return false;
        }

        Player player;
        String warpID = args[0];
        Warp warp = getPlugin().getClassManager().getWarpManager().getWarp(warpID);

        if (warp == null) {
            getPlugin().getUtils().sendMessage(sender, "commands.warp.notFound");
            return false;
        }

        if (args.length == 2) {
            player = getPlugin().getServer().getPlayer(args[1]);
            if (player == null) {
                getPlugin().getUtils().sendMessage(sender, "playerNotExist");
                return false;
            }
        } else {
            if (!(sender instanceof Player)) {
                getPlugin().getUtils().sendMessage(sender, "onlyPlayers");
                return false;
            }
            player = (Player) sender;
        }

        warp.teleport(player);
        String msg = getPlugin().getUtils().getLangMSG(sender, "commands.warp.tp");
        getPlugin().getUtils().sendMessage(sender, msg.replace("{warp}", warpID));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            getPlugin().getClassManager().getWarpManager().getWarps().forEach(warp -> list.add(warp.getName()));
            return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
        }

        if (args.length == 2) {
            List<String> list = new ArrayList<>();
            for (Player target : getPlugin().getServer().getOnlinePlayers()) {
                list.add(target.getName());
            }
            return StringUtil.copyPartialMatches(args[1], list, new ArrayList<>());
        }
        return new ArrayList<>();
    }
}
