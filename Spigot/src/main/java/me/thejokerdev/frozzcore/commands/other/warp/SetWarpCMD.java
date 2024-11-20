package me.thejokerdev.frozzcore.commands.other.warp;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.type.CustomCMD;
import me.thejokerdev.frozzcore.type.Warp;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetWarpCMD extends CustomCMD {
    public SetWarpCMD(SpigotMain plugin) {
        super(plugin);

        setName("setwarp");
        setPermission("frozzcore.setwarp");
        setTabComplete(true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            getPlugin().getUtils().sendMessage(sender, "noPermission");
            return false;
        }

        if (!(sender instanceof Player)) {
            getPlugin().getUtils().sendMessage(sender, "onlyPlayers");
            return false;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            getPlugin().getUtils().sendMessage(sender, "commands.setwarp.help");
            return false;
        }

        String warpID = args[0];
        Warp warp = getPlugin().getClassManager().getWarpManager().getWarp(warpID);

        if (warp != null) {
            getPlugin().getUtils().sendMessage(sender, "commands.setwarp.alreadyExists");
            return false;
        }

        Warp.Builder builder = new Warp.Builder();
        builder.name(warpID);

        if (args.length > 1) {
            String permission = args[1];
            builder.permission(permission);
        }

        if (args.length > 2) {
            int cost = Integer.parseInt(args[2]);
            builder.cost(cost);
        }

        builder.location(player.getLocation());

        warp = builder.build();
        warp.save();

        String msg = getPlugin().getUtils().getLangMSG(sender, "commands.setwarp.success");
        getPlugin().console("{prefix}" + msg);
        getPlugin().getUtils().sendMessage(sender, msg.replace("{warp}", warpID));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
