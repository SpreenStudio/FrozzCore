package me.thejokerdev.frozzcore.commands.admin;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.enums.SenderType;
import me.thejokerdev.frozzcore.type.CMD;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SendPlayerCMD extends CMD {
    public SendPlayerCMD(SpigotMain plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "sendplayer";
    }

    @Override
    public SenderType getSenderType() {
        return SenderType.PLAYER;
    }

    @Override
    public String getPermission() {
        return "frozzcore.sendplayer";
    }

    @Override
    public String getHelp() {
        return "none";
    }

    @Override
    public boolean onCMD(CommandSender sender, String alias, String[] args) {
        Player p = (Player) sender;
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /sendplayer <player> <server>");
            return true;
        }
        String player = args[0];
        String server = args[1];

        if (Bukkit.getPlayer(player) == null) {
            sender.sendMessage("§cPlayer not found");
            return true;
        }

        getPlugin().getPluginMessageManager().connectPlayer(p.hasPermission(getPermission()+".admin") ? Bukkit.getPlayer(player): p, server);

        return true;
    }

    @Override
    public List<String> onTab(CommandSender sender, String alias, String[] args) {
        return null;
    }
}
