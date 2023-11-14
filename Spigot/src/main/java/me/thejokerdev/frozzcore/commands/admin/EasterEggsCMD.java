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
        return "easterEggs";
    }

    @Override
    public SenderType getSenderType() {
        return SenderType.PLAYER;
    }

    @Override
    public String getPermission() {
        return "core.admin.easterEggs";
    }

    @Override
    public String getHelp() {
        return "commands.easterEggs.help";
    }

    //core easterEggs addEasterEgg
    //core easterEggs removeEasterEgg
    //core easterEggs clear <player>

    @Override
    public boolean onCMD(CommandSender sender, String alias, String[] args) {
        Player player = (Player) sender;
        boolean disabled = getPlugin().getClassManager().getEasterEggManager() == null;

        if (disabled) {
            player.sendMessage("Los easterEggs están deshabilitados");
            return true;
        }

        if(args.length == 0) {
            getPlugin().getUtils().sendMessage(sender, getHelp());
            return true;
        }

        if (args[0].equalsIgnoreCase("addEasterEgg"))
            getPlugin().getClassManager().getEasterEggManager().addEasterEgg(player.getLocation());
        if (args[0].equalsIgnoreCase("removeEasterEgg"))
            getPlugin().getClassManager().getEasterEggManager().removeEasterEgg(player.getLocation());
        if (args[0].equalsIgnoreCase("clear")) {
            if(args.length != 2){
                sender.sendMessage("¡Argumentos insuficientes!");
                return true;
            }

            String playerName = args[1];
            PlayerObject cPlayer = CloudAPI.getUniversalAPI().getPlayer(playerName);

            if(cPlayer == null || !cPlayer.isOnline()){
                cPlayer.sendMessage("El jugador "+playerName+" se encuentra desconectado!.");
                return true;
            }

            UUID uuid = cPlayer.getUuid();
            getPlugin().getClassManager().getEasterEggManager().removeAllEasterEggsForPlayer(uuid);
        }
        return true;
    }

    @Override
    public List<String> onTab(CommandSender sender, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1){
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("addEasterEgg", "removeEasterEgg", "clear"), list);
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
