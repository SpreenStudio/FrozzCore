package me.thejokerdev.frozzcore.commands.other;

import lombok.Getter;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.enums.ModifierStatus;
import me.thejokerdev.frozzcore.enums.Modules;
import me.thejokerdev.frozzcore.type.CustomCMD;
import me.thejokerdev.frozzcore.type.FUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

@Getter
public class FlyCMD extends CustomCMD {
    private final SpigotMain plugin;

    public FlyCMD(SpigotMain plugin) {
        super(plugin);
        setName("fly");
        addAliases("volar", "flight");
        setPermission("core.fly");
        setDescription("Usa este comando para volar.");
        setTabComplete(true);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            getPlugin().getClassManager().getUtils().sendMessage(sender, "onlyPlayers");
            return true;
        }
        Player p = (Player) sender;
        FUser user = plugin.getClassManager().getPlayerManager().getUser(p);
        if (!p.hasPermission(getPermission())){
            getPlugin().getClassManager().getUtils().sendMessage(sender, "noPermission");
            return true;
        }
        if (args.length == 0){
            if (!plugin.getUtils().isWorldProtected(p.getWorld(), Modules.FLY)){
                plugin.getUtils().sendMessage(sender, "{prefix}&cNo puedes usar ese comando en el mundo.");
                return true;
            }
            if (user.getAllowFlight() == ModifierStatus.ON){
                getPlugin().getClassManager().getUtils().sendMessage(sender, "commands.fly.deactivated");
                user.setAllowFlight(ModifierStatus.OFF);
            } else {
                getPlugin().getClassManager().getUtils().sendMessage(sender, "commands.fly.activated");
                user.setAllowFlight(ModifierStatus.ON);
                if (p.getAllowFlight()){
                    p.setFlying(true);
                }
            }
            user.saveData(false);
            return true;
        }
        if (args.length == 1){
            if (!p.hasPermission(getPermission()+".others")){
                getPlugin().getClassManager().getUtils().sendMessage(sender, "noPermission");
                return true;
            }
            if (args[0].equals("@a") || args[0].equals("all")){
                if (!p.hasPermission(getPermission()+".all")){
                    getPlugin().getClassManager().getUtils().sendMessage(sender, "noPermission");
                    return true;
                }
                for (Player target : plugin.getServer().getOnlinePlayers()){
                    FUser targetUser = plugin.getClassManager().getPlayerManager().getUser(target);
                    if (!plugin.getUtils().isWorldProtected(target.getWorld(), Modules.FLY)){
                        plugin.getUtils().sendMessage(sender, "{prefix}&cNo puedes usar ese comando en el mundo.");
                        return true;
                    }
                    if (targetUser.getAllowFlight() == ModifierStatus.ON){
                        getPlugin().getClassManager().getUtils().sendMessage(sender, "commands.fly.deactivated");
                        targetUser.setAllowFlight(ModifierStatus.OFF);
                    } else {
                        getPlugin().getClassManager().getUtils().sendMessage(sender, "commands.fly.activated");
                        targetUser.setAllowFlight(ModifierStatus.ON);
                        if (target.getAllowFlight()){
                            target.setFlying(true);
                        }
                    }
                    targetUser.saveData(false);
                }
                plugin.getUtils().sendMessage(sender, "{prefix}Activaste el modo de vuelo a &etodos&7.");
                return true;
            }
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null){
                getPlugin().getClassManager().getUtils().sendMessage(sender, "playerNotFound");
                return true;
            }
            FUser targetUser = plugin.getClassManager().getPlayerManager().getUser(target);
            if (!plugin.getUtils().isWorldProtected(target.getWorld(), Modules.FLY)){
                plugin.getUtils().sendMessage(sender, "{prefix}&cNo puedes usar ese comando en el mundo.");
                return true;
            }
            if (targetUser.getAllowFlight() == ModifierStatus.ON){
                getPlugin().getClassManager().getUtils().sendMessage(sender, "commands.fly.deactivated");
                targetUser.setAllowFlight(ModifierStatus.OFF);
            } else {
                getPlugin().getClassManager().getUtils().sendMessage(sender, "commands.fly.activated");
                targetUser.setAllowFlight(ModifierStatus.ON);
                if (target.getAllowFlight()){
                    target.setFlying(true);
                }
            }
            targetUser.saveData(false);
            plugin.getUtils().sendMessage(target, "{prefix}Activaste el modo de vuelo a &e"+target.getName()+"&7.");
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission(getPermission()+".others")){
            List<String> list = new ArrayList<>();
            for (Player target : plugin.getServer().getOnlinePlayers()){
                list.add(target.getName());
            }
            if (sender.hasPermission(getPermission()+".all")){
                list.add("@a");
                list.add("all");
            }
            return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
        }
        return new ArrayList<>();
    }
}
