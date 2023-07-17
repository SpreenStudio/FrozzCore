package me.thejokerdev.frozzcore.commands.user;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.type.CustomCMD;
import me.thejokerdev.frozzcore.type.FUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoinsCMD extends CustomCMD {
    public CoinsCMD(SpigotMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "coins";
    }

    @Override
    public String getPermission() {
        return "none";
    }

    @Override
    public List<String> getAliases() {
        return new ArrayList<>(Collections.singleton("spreencoins"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            getPlugin().getUtils().sendMessage(sender, "&cThis command is only for players.");
            return false;
        }
        Player player = (Player) sender;
        FUser user = getPlugin().getClassManager().getPlayerManager().getUser(player);
        if (args.length == 0){
            user.sendMSGWithObjets("%core_general_commands_coins_get_self%", String.valueOf(user.getMoney()));
            return true;
        }
        String var1 = args[0];
        Player t = getPlugin().getServer().getPlayer(var1);
        if (t == null){
            user.sendMSGWithObjets("%core_general_playerNotExist%");
            return true;
        }
        FUser target = getPlugin().getClassManager().getPlayerManager().getUser(t);
        user.sendMSGWithObjets("%core_general_commands_coins_get_other%", target.getName(), String.valueOf(target.getMoney()));
        return true;
    }

    @Override
    public boolean isTabComplete() {
        return false;
    }
}
