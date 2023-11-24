package me.thejokerdev.frozzcore.cmds.internal;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.cmds.CMD;
import me.thejokerdev.frozzcore.managers.Permissions;
import me.thejokerdev.frozzcore.utils.StringUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class PingCMD extends CMD {

    public PingCMD(BungeeMain plugin) {
        super(plugin, "ping", "latencia");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 && sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer) sender;
            String msg = plugin.getMessages().getString("cmds.ping.response");
            msg = msg.replace("{ping}", p.getPing()+"");
            plugin.getUtils().sendMessage(sender, msg);
        } else if (args.length == 1){
            String msg = plugin.getMessages().getString("cmds.ping.response-other");
            ProxiedPlayer p = plugin.getProxy().getPlayer(args[0]);
            if (p == null){
                plugin.getUtils().sendMessage(sender, "general.playerNotExists");
                return;
            }
            msg = msg.replace("{ping}", p.getPing()+"");
            msg = msg.replace("{player}", p.getName());
            plugin.getUtils().sendMessage(sender, msg);
        }
    }

    @Override
    public String name() {
        return "ping";
    }

    @Override
    public String permission() {
        return null;
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender.hasPermission(Permissions.PROXYUTILS_ADMIN.get())){
            if (args.length == 1){
                return StringUtil.copyPartialMatches(args[0], plugin.getProxy().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList()), new ArrayList<>());
            }
        }
        return super.onTabComplete(sender, args);
    }
}
