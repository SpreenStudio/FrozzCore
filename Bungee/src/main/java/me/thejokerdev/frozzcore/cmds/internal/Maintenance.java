package me.thejokerdev.frozzcore.cmds.internal;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.cmds.CMD;
import me.thejokerdev.frozzcore.groups.ServersGroup;
import me.thejokerdev.frozzcore.utils.StringUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Maintenance extends CMD {

    public Maintenance(BungeeMain plugin) {
        super(plugin, "maintenance", "mantenimiento");
    }

    @Override
    public String name() {
        return "mantenimiento";
    }

    @Override
    public String permission() {
        return "proxyutils.maintenance.admin";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!check(sender, permission())){
            plugin.getUtils().sendMessage(sender, "general.noPermissions");
            return;
        }
        List<String> list = plugin.getConfig().getStringList("maintenance.whitelist");
        if (args.length == 2){
            if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off")){
                String add = plugin.getMessages().getString("cmds.maintenance.servers.enabled");
                String remove = plugin.getMessages().getString("cmds.maintenance.servers.disabled");
                String server = args[1];
                boolean isServerGroup = this.plugin.getGroupsManager().isServerGroup(server);
                if (!isServerGroup && this.plugin.getProxy().getServers().get(server) == null) {
                    this.plugin.getUtils().sendMessage(sender, "cmds.maintenance.servers.notFound");
                    return;
                }
                List<String> servers = new ArrayList<>(plugin.getConfig().getStringList("maintenance.servers"));
                if (servers.contains(server)) {
                    servers.remove(server);
                    this.plugin.getUtils().sendMessage(sender, remove.replace("{server}", !isServerGroup ? server : this.plugin.getGroupsManager().getGroup(server).getDisplayName()));
                } else {
                    servers.add(server);
                    for (ProxiedPlayer p : this.plugin.getProxy().getPlayers()) {
                        if (!isServerGroup) {
                            if (!p.getServer().getInfo().getName().equals(server)) continue;
                        } else {
                            ServersGroup group = this.plugin.getGroupsManager().getGroup(server);
                            if (!group.isServerInGroup(p.getServer().getInfo())) continue;
                        }
                        if (!list.isEmpty() && list.contains(p.getName().toLowerCase())) continue;
                        if (p.hasPermission("frozzcore.maintenance.bypass") || p.hasPermission("frozzcore.maintenance.bypass." + server)) continue;

                        String msg = this.plugin.getConfig().getString("maintenance.kick-msg");
                        p.disconnect(this.plugin.getUtils().getMSG(p, msg));
                    }
                    this.plugin.getUtils().sendMessage(sender, add.replace("{server}", !isServerGroup ? server : this.plugin.getGroupsManager().getGroup(server).getDisplayName()));
                }
                plugin.getConfig().set("maintenance.servers", servers);
                plugin.saveConfig();
                return;
            }
        }
        if (args.length == 1 || args.length == 2){
            String arg = args[0].toLowerCase();
            if (arg.equals("serverlist") || arg.equals("svlist")){
                String svList = plugin.getMessages().getString("cmds.maintenance.servers.list");
                List<String> servers = new ArrayList<>(plugin.getConfig().getStringList("maintenance.servers"));
                if (servers.isEmpty()){
                    plugin.getUtils().sendMessage(sender, "cmds.maintenance.servers.empty");
                    return;
                }
                svList = svList.replace("{servers}", String.join(", ", servers));
                plugin.getUtils().sendMessage(sender, svList);
                return;
            }
            if (arg.equals("toggle") || (arg.equals("on") || arg.equals("off"))) {
                boolean b = !plugin.getConfig().getBoolean("maintenance.enabled");
                if (arg.equals("on")){
                    b = true;
                } else if (arg.equals("off")){
                    b = false;
                }
                if (b) {
                    plugin.getUtils().sendMessage(sender, "cmds.maintenance.enabled");
                    for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                        if (!list.isEmpty() && list.contains(p.getName().toLowerCase())) {
                            continue;
                        }
                        if (p.hasPermission("frozzcore.maintenance.bypass")) {
                            continue;
                        }
                        String msg = plugin.getConfig().getString("maintenance.kick-msg");
                        p.disconnect(plugin.getUtils().getMSG(p, msg));
                    }
                } else {
                    plugin.getUtils().sendMessage(sender, "cmds.maintenance.disabled");
                }
                plugin.getConfig().set("maintenance.enabled", b);
                plugin.getFileUtils().saveConfig();
                return;
            }
        }
        if (args.length == 3){
            String already = plugin.getFileUtils().getMessages().getString("cmds.maintenance.already");
            String added = plugin.getFileUtils().getMessages().getString("cmds.maintenance.added");
            String removed = plugin.getFileUtils().getMessages().getString("cmds.maintenance.removed");
            String notFound = plugin.getFileUtils().getMessages().getString("cmds.maintenance.not-found");
            String var1 = args[0].toLowerCase();
            String var2 = args[1].toLowerCase();
            String var3 = args[2];

            if (var1.equals("whitelist")){
                if (var2.equals("add")){
                    if (list.contains(var3.toLowerCase())){
                        already = already.replace("{name}", var3);
                        plugin.getUtils().sendMessage(sender, already);
                        return;
                    }
                    list.add(var3.toLowerCase());
                    added = added.replace("{name}", var3);
                    plugin.getConfig().set("maintenance.whitelist", list);
                    plugin.getFileUtils().saveConfig();
                    plugin.getUtils().sendMessage(sender, added);
                    return;
                } else if (var2.equals("remove")){
                    if (!list.contains(var3.toLowerCase())){
                        notFound = notFound.replace("{name}", var3);
                        plugin.getUtils().sendMessage(sender, notFound);
                        return;
                    }
                    list.remove(var3.toLowerCase());
                    removed = removed.replace("{name}", var3);
                    plugin.getConfig().set("maintenance.whitelist", list);
                    plugin.getFileUtils().saveConfig();
                    plugin.getUtils().sendMessage(sender, removed);
                    return;
                }
            }
        }

        plugin.getUtils().sendMessage(sender, "cmds.maintenance.help");
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!justCheck(sender, permission())){
            return new ArrayList<>();
        }
        List<String> list = new ArrayList<>();
        if (args.length == 1){
            return StringUtil.copyPartialMatches(args[0], getListOf("toggle", "whitelist", "svlist", "serverlist", "on", "off"), list);
        }
        if (args.length == 2){
            if (args[0].equalsIgnoreCase("toggle")){
                return StringUtil.copyPartialMatches(args[1], plugin.getProxy().getServers().keySet(), list);
            }
            if (args[0].equalsIgnoreCase("on")){
                List<String> proxy_servers = new ArrayList<>(plugin.getProxy().getServers().keySet());
                List<String> config_servers = plugin.getConfig().getStringList("maintenance.servers");
                proxy_servers.removeAll(config_servers);
                return StringUtil.copyPartialMatches(args[1], proxy_servers, list);
            }
            if (args[0].equalsIgnoreCase("off")){
                return StringUtil.copyPartialMatches(args[1], plugin.getConfig().getStringList("maintenance.servers"), list);
            }
            if (args[0].equalsIgnoreCase("whitelist")){
                return StringUtil.copyPartialMatches(args[1], getListOf("add", "remove"), list);
            }
        }
        if (args.length == 3){
            List<String> whitelist = plugin.getConfig().getStringList("maintenance.whitelist");

            if (args[0].equalsIgnoreCase("whitelist")){
                if (args[1].equalsIgnoreCase("add")) {
                    return StringUtil.copyPartialMatches(args[2], plugin.getProxy().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList()), list);
                }
                if (args[1].equalsIgnoreCase("remove")) {
                    return StringUtil.copyPartialMatches(args[2].toLowerCase(), whitelist, list);
                }
            }
        }
        return list;
    }
}
