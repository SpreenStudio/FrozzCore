package me.thejokerdev.frozzcore.commands.admin;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.enums.SenderType;
import me.thejokerdev.frozzcore.type.CMD;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import studio.spreen.cloud.api.CloudAPI;
import studio.spreen.cloud.api.objects.PlayerObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EasterEggsCMD extends CMD {

    public EasterEggsCMD(SpigotMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "eastereggs";
    }

    @Override
    public SenderType getSenderType() {
        return SenderType.PLAYER;
    }

    @Override
    public String getPermission() {
        return "core.admin.eastereggs";
    }

    @Override
    public String getHelp() {
        return "commands.eastereggs.help";
    }

    //core eastereggs addeasteregg
    //core eastereggs removeeasteregg
    //core eastereggs clear <player>

    @Override
    public boolean onCMD(CommandSender sender, String alias, String[] args) {
        Player p = (Player) sender;
        boolean easterEggsEnable = getPlugin().getClassManager().getEasterEggManager() != null;

        if(!easterEggsEnable){
            p.sendMessage("Los eastereggs estan desactivados");
            return true;
        }

        if(args.length == 0){
            getPlugin().getUtils().sendMessage(sender, getHelp());
            return true;
        }

        switch(args[0]){
            case "addeasteregg":{
                getPlugin().getClassManager().getEasterEggManager().addEasterEgg(p.getLocation());
                break;
            }
            case "removeeasteregg":{
                getPlugin().getClassManager().getEasterEggManager().removeEasterEgg(p.getLocation());
                break;
            }
            case "clear":{
                if(args.length != 2){
                    //getPlugin().getUtils().sendMessage(sender, getHelp());
                    sender.sendMessage("argumentos insuficientes!");
                    return true;
                }

                String playerName = args[1];
                PlayerObject player = CloudAPI.getUniversalAPI().getPlayer(playerName);

                if(player == null || !player.isOnline()){
                    p.sendMessage("El jugador "+playerName+" se encuentra desconectado!.");
                    return true;
                }

                UUID uuid = player.getUuid();
                getPlugin().getClassManager().getEasterEggManager().removeAllEasterEggsForPlayer(uuid);
            }
        }

        return true;
    }

    @Override
    public List<String> onTab(CommandSender sender, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1){
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("addeasteregg", "removeeasteregg", "clear"), list);
        }
        if (args.length == 2){
            String arg1 = args[0].toLowerCase();
            if (arg1.equals("clear")){
                return StringUtil.copyPartialMatches(args[1], Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList()), list);
            }
        }
        return list;
    }
}
