package me.thejokerdev.frozzcore.cmds.internal;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.cmds.CMD;
import me.thejokerdev.frozzcore.utils.StringUtil;
import net.luckperms.api.model.group.Group;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProxyTP extends CMD {

    public ProxyTP(BungeeMain plugin) {
        super(plugin, "proxytp", "ptp", "btp");
    }

    @Override
    public String name() {
        return "proxytp";
    }

    @Override
    public String permission() {
        return "proxyutils.proxytp";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!check(sender, permission())){
            return;
        }
        if (args.length == 0){
            sendHelp(sender);
            return;
        }
        if (args.length == 1){
            if (sender instanceof ProxiedPlayer){
                ProxiedPlayer player = (ProxiedPlayer) sender;
                String server = args[0];
                ServerInfo sv = plugin.getProxy().getServerInfo(server);
                if (sv == null){
                    plugin.getUtils().sendMessage(sender, "cmds.proxytp.notExist");
                    return;
                }
                if (!check(sender, permission()+".server."+server)){
                    return;
                }
                player.connect(sv);
                String msg = plugin.getMessages().getString("cmds.proxytp.tp");
                plugin.getUtils().sendMessage(sender, msg.replace("%server%", sv.getName()));
            } else {
                plugin.getUtils().sendMessage(sender, "general.onlyPlayers");
                return;
            }
        }
        String summon = plugin.getMessages().getString("cmds.proxytp.summon");

        if (args.length == 2){
            if (!check(sender, getPermission()+".others")){
                return;
            }
            String var1 = args[0];
            String var2 = args[1];
            ServerInfo sv = plugin.getProxy().getServerInfo(var1);
            ServerInfo sv2;
            if (sv == null){
                if (plugin.getProxy().getPlayer(var1)!=null){
                    ProxiedPlayer p = plugin.getProxy().getPlayer(var1);
                    sv2 = plugin.getProxy().getServerInfo(var2);
                    if (sv2 == null) {
                        plugin.getUtils().sendMessage(sender, "cmds.proxytp.notExist");
                        return;
                    }
                    p.connect(sv2);
                    String msg = plugin.getMessages().getString("cmds.proxytp.tp");
                    plugin.getUtils().sendMessage(p, msg.replace("%server%", sv2.getName()));
                    summon = summon.replace("{amount}", "1");
                    summon = summon.replace("{server}", sv2.getName());
                    plugin.getUtils().sendMessage(sender, summon);
                    return;
                }
                if (plugin.getApi().getGroupManager().getGroup(var1)!=null) {
                    Group group = plugin.getApi().getGroupManager().getGroup(var1);
                    sv2 = plugin.getProxy().getServerInfo(var2);
                    if (sv2 == null || group == null) {
                        plugin.getUtils().sendMessage(sender, "cmds.proxytp.notExist");
                        return;
                    }
                    int i = 0;
                    for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                        if (plugin.getGroup(p).equals(group.getName())) {
                            p.connect(sv2);
                            String msg = plugin.getMessages().getString("cmds.proxytp.tp");
                            plugin.getUtils().sendMessage(p, msg.replace("%server%", sv2.getName()));
                            i++;
                        }
                    }
                    summon = summon.replace("{amount}", i+"");
                    summon = summon.replace("{server}", sv2.getName());
                    plugin.getUtils().sendMessage(sender, summon);
                    return;
                }
                if (var1.equalsIgnoreCase("all")) {
                    sv2 = plugin.getProxy().getServerInfo(var2);
                    if (sv2 == null) {
                        plugin.getUtils().sendMessage(sender, "cmds.proxytp.notExist");
                        return;
                    }
                    int i = 0;
                    for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                        p.connect(sv2);
                        String msg = plugin.getMessages().getString("cmds.proxytp.tp");
                        plugin.getUtils().sendMessage(p, msg.replace("%server%", sv2.getName()));
                        i++;
                    }
                    summon = summon.replace("{amount}", i+"");
                    summon = summon.replace("{server}", sv2.getName());
                    plugin.getUtils().sendMessage(sender, summon);
                    return;
                }
            } else {
                sv2 = plugin.getProxy().getServerInfo(var2);
                if (sv2 == null) {
                    plugin.getUtils().sendMessage(sender, "cmds.proxytp.notExist");
                    return;
                }
                for (ProxiedPlayer p : sv.getPlayers()) {
                    p.connect(sv2);
                    String msg = plugin.getMessages().getString("cmds.proxytp.tp");
                    plugin.getUtils().sendMessage(p, msg.replace("%server%", sv2.getName()));
                }
                summon = summon.replace("{amount}", sv.getPlayers().size()+"");
                summon = summon.replace("{server}", sv2.getName());
                plugin.getUtils().sendMessage(sender, summon);
                return;
            }

            sendHelp(sender);
        }
    }

    public void sendHelp(CommandSender sender){
        plugin.getUtils().sendMessage(sender, "cmds.proxytp.help");
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();

        if (!justCheck(sender, permission())){
            return list;
        }
        if (args.length == 1){
            String var1 = args[0];
            for (String server : plugin.getProxy().getServers().values().stream().map(ServerInfo::getName).collect(Collectors.toList())){
                if (justCheck(sender, permission()+".server."+server)){
                    list2.add(server);
                }
            }
            if (justCheck(sender, getPermission()+".others")){
                list2.addAll(plugin.getApi().getGroupManager().getLoadedGroups().stream().map(Group::getName).collect(Collectors.toList()));
                list2.addAll(plugin.getProxy().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList()));
                list2.add("all");
            }

            return StringUtil.copyPartialMatches(var1, list2, list);
        }
        if (args.length == 2){
            String var2 = args[1];
            if (!justCheck(sender, getPermission()+".others")){
                return list;
            }
            list2.addAll(plugin.getProxy().getServers().values().stream().map(ServerInfo::getName).collect(Collectors.toList()));
            return StringUtil.copyPartialMatches(var2, list2, list);
        }
        return list;
    }
}
