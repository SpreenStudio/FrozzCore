package me.thejokerdev.frozzcore.commands.other;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.type.CustomCMD;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class SpeedCMD extends CustomCMD {
    public SpeedCMD(SpigotMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "speed";
    }

    @Override
    public String getPermission() {
        return "frozzcore.command.speed";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            getPlugin().getUtils().sendMessage(sender, "noPermission");
            return true;
        }

        if (!(sender instanceof Player)) {
            getPlugin().getClassManager().getUtils().sendMessage(sender, "onlyPlayers");
            return true;
        }

        Player p = (Player) sender;

        if (args.length == 0) {
            getPlugin().getClassManager().getUtils().sendMessage(sender, "commands.speed.help");
            return true;
        }

        String var1 = args[0];
        if (var1.equalsIgnoreCase("help")) {
            getPlugin().getClassManager().getUtils().sendMessage(sender, "commands.speed.help");
            return true;
        }

        if (var1.equalsIgnoreCase("reset")) {
            if (args.length == 2) {
                if (!sender.hasPermission("frozzcore.command.speed.others")) {
                    getPlugin().getClassManager().getUtils().sendMessage(sender, "noPermission");
                    return true;
                }
                String name = args[1];
                Player target = getPlugin().getServer().getPlayer(name);
                if (target == null) {
                    getPlugin().getClassManager().getUtils().sendMessage(sender, "playerNotExist");
                    return true;
                }
                String msg = getPlugin().getClassManager().getUtils().getLangMSG(p, "commands.speed.resetOther");
                getPlugin().getClassManager().getUtils().sendMessage(sender, String.format(msg, target.getName()));
                p = target;
            }

            boolean silent = args.length == 2 && args[1].equalsIgnoreCase("-s");
            p.setWalkSpeed(0.2F);
            p.setFlySpeed(0.1F);
            if (!silent) getPlugin().getClassManager().getUtils().sendMessage(p, "commands.speed.reset");
            return true;
        }

        double speed;

        try {
            speed = Double.parseDouble(var1);
        } catch (NumberFormatException e) {
            getPlugin().getClassManager().getUtils().sendMessage(sender, "commands.speed.invalidSpeed");
            return true;
        }

        if (speed < 0 || speed > 10) {
            getPlugin().getClassManager().getUtils().sendMessage(sender, "commands.speed.invalidSpeed");
            return true;
        }

        //convert speed to float
        //default player speed is 0.2f so 1 == 0.2f
        float fSpeed = (float) (speed / (p.isFlying() ? 10 : 5));
        if (args.length == 2) {
            if (!sender.hasPermission("frozzcore.command.speed.others")) {
                getPlugin().getClassManager().getUtils().sendMessage(sender, "noPermission");
                return true;
            }
            String name = args[1];
            Player target = getPlugin().getServer().getPlayer(name);
            if (target == null) {
                getPlugin().getClassManager().getUtils().sendMessage(sender, "playerNotExist");
                return true;
            }
            String msg = getPlugin().getClassManager().getUtils().getLangMSG(p, "commands.speed.setSpeedOther");
            getPlugin().getClassManager().getUtils().sendMessage(sender, String.format(msg, speed, target.getName()));
            p = target;
        }

        boolean silent = args.length == 3 && args[2].equalsIgnoreCase("-s");

        String msg = p.isFlying() ? "commands.speed.setFlySpeed" : "commands.speed.setWalkSpeed";
        msg = getPlugin().getClassManager().getUtils().getLangMSG(p, msg);
        if (p.isFlying()) {
            p.setFlySpeed(fSpeed);
        } else {
            p.setWalkSpeed(fSpeed);
        }
        if (!silent) getPlugin().getClassManager().getUtils().sendMessage(sender, String.format(msg, speed));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("reset");
            list.add("help");
            for (int i = 0; i <= 10; i++) {
                list.add(String.valueOf(i));
            }
            return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("help")) {
                return new ArrayList<>();
            }
            if (sender.hasPermission("frozzcore.command.speed.others")) {
                List<String> players = new ArrayList<>();
                for (Player p : getPlugin().getServer().getOnlinePlayers()) {
                    players.add(p.getName());
                }
                return StringUtil.copyPartialMatches(args[1], players, new ArrayList<>());
            }
        }

        return new ArrayList<>();
    }
}
