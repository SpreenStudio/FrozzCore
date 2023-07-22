package me.thejokerdev.frozzcore.commands.other;

import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.type.CustomCMD;
import me.thejokerdev.frozzcore.type.FUser;
import me.thejokerdev.frozzcore.type.NickData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NickCMD extends CustomCMD {
    public NickCMD(SpigotMain plugin) {
        super(plugin);

        setName("nick");
        addAliases("nickname");
        setPermission("core.nick");
        setDescription("Usa este comando para cambiar tu nick.");
        setTabComplete(true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            getPlugin().getUtils().sendMessage(sender, "onlyPlayers");
        }

        if (!sender.hasPermission(getPermission())){
            getPlugin().getUtils().sendMessage(sender, "noPermission");
            return true;
        }

        Player p = (Player) sender;
        FUser user = getPlugin().getClassManager().getPlayerManager().getUser(p);

        if (args.length == 0){
            if (user.isNicked()){
                getPlugin().getUtils().sendMessage(sender, "commands.nick.isNicked");
                return true;
            }
            //TODO: Open GUI to select a nick
            getPlugin().getUtils().sendMessage(sender, "maintenance");
            return true;
        }

        if (args.length == 1){
            String var1 = args[0];
            if (var1.equalsIgnoreCase("help")){
                getPlugin().getUtils().sendMessage(sender, "commands.nick.help");
                return true;
            }

            if (var1.equalsIgnoreCase("clear")){
                if (!user.isNicked()){
                    getPlugin().getUtils().sendMessage(sender, "commands.nick.notNicked");
                    return true;
                }
                user.setNicked(false);
                getPlugin().getUtils().sendMessage(sender, "commands.nick.clear");
                return true;
            }

            if (user.isNicked()) {
                getPlugin().getUtils().sendMessage(sender, "commands.nick.isNicked");
                return true;
            }

            NickData data = new NickData(getPlugin(), user, var1);
            if (!data.isValid()){
                getPlugin().getUtils().sendMessage(sender, "commands.nick.invalidNick");
                return true;
            }
            user.setNickData(data);
            user.setNicked(true);
            getPlugin().getUtils().sendMessage(sender, "commands.nick.setNick", true, var1);
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission(getPermission())){
            return new ArrayList<>();
        }
        if (args.length == 1){
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("help", "clear"), new ArrayList<>());
        }
        return new ArrayList<>();
    }
}
