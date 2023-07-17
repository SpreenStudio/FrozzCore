package me.thejokerdev.frozzcore.commands.admin;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.enums.SenderType;
import me.thejokerdev.frozzcore.type.CMD;
import me.thejokerdev.frozzcore.type.FUser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class MoneyCMD extends CMD {
    public MoneyCMD(SpigotMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "money";
    }

    @Override
    public SenderType getSenderType() {
        return SenderType.BOTH;
    }

    @Override
    public String getPermission() {
        return "core.admin.money";
    }

    @Override
    public String getHelp() {
        return "commands.coins.help";
    }

    @Override
    public boolean onCMD(CommandSender sender, String alias, String[] args) {
        if (args.length == 0){
            getPlugin().getClassManager().getUtils().sendMessage(sender, "commands.coins.help");
            return false;
        }
        boolean isConsole = !(sender instanceof Player);
        String var1 = args[0].toLowerCase();
        String name = args[1].toLowerCase();
        if (isConsole && args.length == 2){
            getPlugin().getClassManager().getUtils().sendMessage(sender, "commands.coins.help");
            return false;
        }
        String amount = args.length == 3 ? args[2] : args[1];
        try {
            Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            getPlugin().getUtils().sendMessage(sender, "onlyIntegers", true);
            return false;
        }
        double coins = Double.parseDouble(amount);
        switch (var1){
            case "add":{
                if (args.length == 2){
                    Player player = getPlugin().getServer().getPlayer(name);
                    FUser user = getPlugin().getClassManager().getPlayerManager().getUser(player);
                    user.addMoney(coins);
                    getPlugin().getUtils().sendMessage(sender, "commands.coins.give.self", true, String.valueOf(coins));
                    return true;
                }
                if (args.length == 3){
                    Player player = getPlugin().getServer().getPlayer(name);
                    FUser user = getPlugin().getClassManager().getPlayerManager().getUser(player);
                    user.addMoney(coins);
                    getPlugin().getUtils().sendMessage(sender, "commands.coins.give.other", true, String.valueOf(coins), player.getName());
                    return true;
                }
                break;
            }
            case "remove":{
                if (args.length == 2){
                    Player player = getPlugin().getServer().getPlayer(name);
                    FUser user = getPlugin().getClassManager().getPlayerManager().getUser(player);
                    user.removeMoney(coins);
                    getPlugin().getUtils().sendMessage(sender, "commands.coins.remove.self", true, String.valueOf(coins));
                    return true;
                }
                if (args.length == 3){
                    Player player = getPlugin().getServer().getPlayer(name);
                    FUser user = getPlugin().getClassManager().getPlayerManager().getUser(player);
                    user.removeMoney(coins);
                    getPlugin().getUtils().sendMessage(sender, "commands.coins.remove.other", true, String.valueOf(coins), player.getName());
                    return true;
                }
                break;
            }
            case "set":{
                if (args.length == 2){
                    Player player = getPlugin().getServer().getPlayer(name);
                    FUser user = getPlugin().getClassManager().getPlayerManager().getUser(player);
                    user.setMoney(coins, true);
                    getPlugin().getUtils().sendMessage(sender, "commands.coins.set.self", true, String.valueOf(coins));
                    return true;
                }
                if (args.length == 3){
                    Player player = getPlugin().getServer().getPlayer(name);
                    FUser user = getPlugin().getClassManager().getPlayerManager().getUser(player);
                    user.setMoney(coins, true);
                    getPlugin().getUtils().sendMessage(sender, "commands.coins.set.other", true, String.valueOf(coins), player.getName());
                    return true;
                }
                break;
            }
        }
        return false;
    }

    @Override
    public List<String> onTab(CommandSender sender, String alias, String[] args) {
        if (args.length == 1){
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("add", "remove", "set"), new ArrayList<>());
        }
        if (args.length == 2){
            return StringUtil.copyPartialMatches(args[1], getPlugin().getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), new ArrayList<>());
        }
        return new ArrayList<>();
    }
}
