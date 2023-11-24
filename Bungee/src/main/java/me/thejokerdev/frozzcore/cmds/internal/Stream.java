package me.thejokerdev.frozzcore.cmds.internal;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.cache.Cache;
import me.thejokerdev.frozzcore.cache.CacheManager;
import me.thejokerdev.frozzcore.cmds.CMD;
import me.thejokerdev.frozzcore.groups.ServersGroup;
import me.thejokerdev.frozzcore.managers.Permissions;
import me.thejokerdev.frozzcore.utils.Platform;
import me.thejokerdev.frozzcore.utils.TimeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Stream extends CMD {
    public Stream(BungeeMain plugin) {
        super(plugin, "stream", (String[])plugin.getConfig().getStringList("stream.aliases").toArray((Object[])new String[0]));
    }

    public String name() {
        return getName();
    }

    public List<String> aliases() {
        return Arrays.stream(getAliases()).collect(Collectors.toList());
    }

    public String permission() {
        return Permissions.STREAMER.get();
    }

    private void sendMSG(ProxiedPlayer p, String url) {
        Platform platform = plugin.getPlatformManager().getPlatform(url);
        String[] msg = plugin.getMessages().getString("stream.msg").split("\\n");
        List<TextComponent> components = new ArrayList<>();
        for (String s : msg) {
            s = s.replace("\\n", "");
            s = plugin.getUtils().ct(s);
            if (s.contains("{player}"))
                s = s.replace("{player}", p.getName());
            if (s.contains("{location}")) {
                ServersGroup group;
                try {
                    group = plugin.getGroupsManager().getGroup(p.getServer().getInfo().getName());
                } catch (Exception e) {
                    group = null;
                }
                if (group == null)
                    group = plugin.getGroupsManager().getGroup("Lobby");
                s = s.replace("{location}", group.getName());
            }
            TextComponent var1 = new TextComponent(plugin.getUtils().getMSG(p, s));
            if (s.contains("{url}") || s.contains("{platform}")) {
                if (s.contains("{url}"))
                    s = s.replace("{url}", url);
                if (s.contains("{platform}"))
                    s = s.replace("{platform}", platform.getDisplayName());
                var1 = new TextComponent(plugin.getUtils().getMSG(p, s));
                var1.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            }
            components.add(var1);
        }
        for (TextComponent component : components) {
            List<ProxiedPlayer> players = plugin.getProxy().getPlayers().stream().filter(t -> {
                if (t.getServer().getInfo() == null)
                    return false;
                ServerInfo serverInfo = t.getServer().getInfo();
                ServersGroup group = plugin.getGroupsManager().getGroup(serverInfo);
                if (group == null)
                    group = plugin.getGroupsManager().getGroup("Lobby");
                return group.isServerInGroup(p.getServer().getInfo());
            }).collect(Collectors.toList());
            if (players.isEmpty())
                players.addAll(plugin.getProxy().getPlayers());
            players.forEach(t -> t.sendMessage(component));
        }
    }

    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            plugin.getUtils().sendMessage(sender, "general.onlyPlayers");
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer)sender;
        if (!check(p, permission())) {
            return;
        }
        if (args.length == 0) {
            plugin.getUtils().sendMessage(p, "stream.usage");
            return;
        }
        String var1 = args[0];
        boolean b = (var1.startsWith("https://") || var1.startsWith("http://"));
        if (!b) {
            plugin.getUtils().sendMessage(p, "stream.incorrect-url");
            return;
        }
        if (!plugin.getPlatformManager().checkPlatform(var1)) {
            plugin.getUtils().sendMessage(p, "stream.invalid-platform");
            return;
        }
        if (p.hasPermission("proxyutils.stream.bypass")) {
            sendMSG(p, var1);
            return;
        }
        Cache cache = CacheManager.getCacheOrGetNew(p.getName());
        long time = plugin.getConfig().getLong("stream.time") * 1000L;
        if (!TimeUtils.elapsed(time, cache.lastUsage)) {
            String msg = plugin.getMessages().getString("stream.cooldown");
            msg = msg.replace("{time}", getTimeFormatted(TimeUtils.left(time, cache.lastUsage)));
            plugin.getUtils().sendMessage(p, msg);
            return;
        }
        cache.setLastUsage(System.currentTimeMillis());
        sendMSG(p, var1);
    }

    public String getTimeFormatted(long time) {
        int var4 = (int)(time / 1000L);
        int var5 = var4 % 86400 % 3600 % 60;
        int var6 = var4 % 86400 % 3600 / 60;
        boolean var9 = true;
        boolean var10 = true;
        if (var5 == 1)
            var9 = false;
        if (var6 == 1)
            var10 = false;
        String var13 = (var5 != 0) ? (var9 ? plugin.getMessages().getString("general.time.seconds") : plugin.getMessages().getString("general.time.second")) : "";
        String var14 = String.format(var13, var5);
        String var15 = var10 ? plugin.getMessages().getString("general.time.minutes") : plugin.getMessages().getString("general.time.minute");
        String var16 = String.format(var15, var6);
        String segundos = "%SECONDS%";
        String minutos = "%MINUTES%";
        if (var6 == 0) {
            var16 = "";
            minutos = "";
        }
        String var1 = minutos + segundos;
        return var1.replaceAll("%SECONDS%", var14).replaceAll("%MINUTES%", var16);
    }
}
