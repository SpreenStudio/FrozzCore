package me.thejokerdev.frozzcore.cmds.internal;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.cmds.CMD;
import me.thejokerdev.frozzcore.managers.Permissions;
import me.thejokerdev.frozzcore.utils.StringUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProxyUtils extends CMD {
    public ProxyUtils(BungeeMain plugin) {
        super(plugin, "proxyutils", "pu");
    }

    @Override
    public String name() {
        return getName();
    }

    @Override
    public List<String> aliases() {
        return Arrays.stream(getAliases()).collect(Collectors.toList());
    }

    @Override
    public String getPermission() {
        return Permissions.PROXYUTILS_ADMIN.get();
    }

    @Override
    public String permission() {
        return getPermission();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())){
            plugin.getUtils().sendMessage(sender, "general.noPermissions");
            return;
        }
        if (args.length == 0){
            plugin.getUtils().sendMessage(sender, "cmds.proxyutils.help");
        }
        if (args.length == 1){
            String arg1 = args[0];
            if (arg1.equalsIgnoreCase("reload")){
                plugin.reloadConfig();
                plugin.getWebhookManager().load();
                plugin.getUtils().sendMessage(sender, "cmds.proxyutils.reload");
                return;
            }
            if (arg1.equalsIgnoreCase("setplayerlimit")){
                plugin.getUtils().sendMessage(sender, "cmds.proxyutils.setplayerlimit.usage");
                return;
            }
            if (sender instanceof ProxiedPlayer){
                ProxiedPlayer p = (ProxiedPlayer) sender;
                if (arg1.equalsIgnoreCase("simulejoin")){
                    String msg = plugin.getUtils().getMSG(p, plugin.getFileUtils().getMessages().getString("staffchat.join")).getText();
                    plugin.getWebhookManager().getStaff().setTitle("Staff join")
                            .setDescription(msg)
                            .setColor("#00ff00")
                            .setTimestamp(true)
                            .execute();
                    return;
                }
                if (arg1.equalsIgnoreCase("simuleleave")){
                    String msg = plugin.getUtils().getMSG(p, plugin.getFileUtils().getMessages().getString("staffchat.leave")).getText();
                    plugin.getWebhookManager().getStaff().setTitle("Staff leave")
                            .setDescription(msg)
                            .setColor("#ff0000")
                            .setTimestamp(true)
                            .execute();
                    return;
                }
            }
            if (arg1.equalsIgnoreCase("setmaxplayers")){
                plugin.getUtils().sendMessage(sender, "cmds.proxyutils.setmaxplayers.usage");
                return;
            }
            if (arg1.equalsIgnoreCase("unbanall")){
                List<String> banned = new ArrayList<>(plugin.getConfig().getStringList("unban-list"));
                String cmd = "unban %player% Unbanned by Console";
                plugin.getProxy().getPluginManager().dispatchCommand(plugin.getProxy().getConsole(), "glist");
                banned.forEach(s -> plugin.getProxy().getPluginManager().dispatchCommand(plugin.getProxy().getConsole(), cmd.replace("%player%", s)));
                plugin.getUtils().sendMessage(sender, "{prefix}&aSe han desbaneado &e"+banned.size()+" &ajugadores.");
                return;
            }
        }
        if (args.length == 2) {
            String arg = args[0].toLowerCase();
            if (arg.equals("setmaxplayers")){
                String max = args[1];
                int maxPlayers;
                try {
                    maxPlayers = Integer.parseInt(max);
                } catch (NumberFormatException e) {
                    plugin.getUtils().sendMessage(sender, "cmds.proxyutils.setmaxplayers.notNumber");
                    return;
                }
                plugin.getConfig().set("motd.max-players", maxPlayers);
                plugin.saveConfig();
                String msg = plugin.getMessages().getString("cmds.proxyutils.setmaxplayers.success");
                msg = msg.replace("{amount}", max);
                plugin.getUtils().sendMessage(sender, msg);
                return;
            }
            if (arg.equals("setplayerlimit")){
                String max = args[1];
                int maxPlayers;
                try {
                    maxPlayers = Integer.parseInt(max);
                } catch (NumberFormatException e) {
                    plugin.getUtils().sendMessage(sender, "cmds.proxyutils.setplayerlimit.notNumber");
                    return;
                }
                try {
                    plugin.changeSlots(maxPlayers);
                } catch (ReflectiveOperationException ignored) {
                }
                plugin.getConfig().set("settings.player-limit", maxPlayers);
                plugin.saveConfig();
                String msg = plugin.getMessages().getString("cmds.proxyutils.setplayerlimit.success");
                msg = msg.replace("{amount}", max);
                plugin.getUtils().sendMessage(sender, msg);
            }
            if (arg.equals("setonlinemode")){
                String str = args[1];
                boolean bool;
                try {
                    bool = Boolean.parseBoolean(str);
                } catch (NumberFormatException e) {
                    plugin.getUtils().sendMessage(sender, "cmds.proxyutils.setonlinemode.usage");
                    return;
                }
                if (bool == plugin.isOnlineMode()){
                    String msg = plugin.getMessages().getString("cmds.proxyutils.setonlinemode.already");
                    msg = msg.replace("{status}", String.valueOf(bool));
                    plugin.getUtils().sendMessage(sender, msg);
                    return;
                }
                plugin.getConfig().set("settings.online-mode", bool);
                plugin.saveConfig();

                String msg = plugin.getMessages().getString("cmds.proxyutils.setonlinemode."+ (bool ? "premium" : "no-premium"));
                plugin.getUtils().sendMessage(sender, msg);
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1){
            StringUtil.copyPartialMatches(args[0], Arrays.asList("reload", "setmaxplayers", "test", "setplayerlimit", "setonlinemode", "unbanall"
                    , "simulejoin", "simuleleave"
            ), list);
            Collections.sort(list);
        }
        if (args.length == 2){
            if (args[0].equalsIgnoreCase("setonlinemode")){
                return StringUtil.copyPartialMatches(args[1], getListOf(!plugin.isOnlineMode()), list);
            }
        }
        return list;
    }
}
