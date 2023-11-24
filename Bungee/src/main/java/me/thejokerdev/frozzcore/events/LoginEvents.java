package me.thejokerdev.frozzcore.events;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.managers.Managers;
import me.thejokerdev.frozzcore.managers.Permissions;
import me.thejokerdev.frozzcore.managers.RedisCacheManager;
import me.thejokerdev.frozzcore.redis.payload.Payload;
import me.thejokerdev.frozzcore.utils.RedisCallable;
import me.thejokerdev.frozzcore.utils.RedisMessage;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LoginEvents implements Listener {
    private final BungeeMain plugin;

    public LoginEvents(BungeeMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PostLoginEvent e){
        ProxiedPlayer p = e.getPlayer();

        if (p.hasPermission(Permissions.STAFFCHAT_JOIN.get())){
            String msg = plugin.getUtils().getMSG(p, plugin.getFileUtils().getMessages().getString("staffchat.join")).getText();
            plugin.getUtils().sendMSGtoStaff(p, msg);
            plugin.getWebhookManager().getStaff().setTitle("Staff join")
                    .setDescription(msg)
                    .setColor("#00ff00")
                    .setTimestamp(true)
                    .execute();
            if (Managers.isHided(p)){
                plugin.getProxy().getScheduler().schedule(plugin, () -> plugin.getUtils().sendMessage(p, "staffchat.hided-reminder"), 1, TimeUnit.SECONDS);
            }
            if (Managers.isToggled(p)){
                plugin.getProxy().getScheduler().schedule(plugin, () -> plugin.getUtils().sendMessage(p, "staffchat.toggled-reminder"), 1, TimeUnit.SECONDS);
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent e){
        ProxiedPlayer p = e.getPlayer();

        if (p.hasPermission(Permissions.STAFFCHAT_LEAVE.get())){
            String msg = plugin.getUtils().getMSG(p, plugin.getFileUtils().getMessages().getString("staffchat.leave")).getText();
            plugin.getUtils().sendMSGtoStaff(p, msg);
            plugin.getWebhookManager().getStaff().setTitle("Staff left")
                    .setDescription(msg)
                    .setColor("#ff0000")
                    .setTimestamp(true)
                    .execute();
        }
    }

    @EventHandler
    public void onJoin(PostLoginEvent event) {
        final ProxiedPlayer player = event.getPlayer();
        plugin.getProxy().getScheduler().runAsync(plugin, new RedisCallable<Void>(plugin) {
            protected Void call(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                RedisCacheManager.get(plugin).createPlayer(player, pipeline);
                plugin.getRedis().write((new RedisMessage(plugin, Payload.JOIN)).setParam("uuid", player.getUniqueId().toString()).setParam("name", player.getName()).setParam("proxy", plugin.getProxyName()).setParam("ip", player.getPendingConnection().getSocketAddress().toString().split("/")[1].split(":")[0]).toJSON());
                pipeline.sync();
                return null;
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        final ProxiedPlayer player = event.getPlayer();
        plugin.getProxy().getScheduler().runAsync(plugin, new RedisCallable<Void>(plugin) {
            protected Void call(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                RedisCacheManager.get(plugin).removePlayer(player.getUniqueId().toString(), player.getName(), pipeline);
                plugin.getRedis().write((new RedisMessage(plugin, Payload.LEAVE)).setParam("uuid", player.getUniqueId().toString()).setParam("name", player.getName()).setParam("proxy", plugin.getProxyName()).toJSON());
                pipeline.sync();
                return null;
            }
        });
    }

    @EventHandler
    public void onServerChange(final ServerConnectedEvent event) {
        plugin.getProxy().getScheduler().runAsync(plugin, new RedisCallable<Void>(plugin) {
            protected Void call(Jedis jedis) {
                jedis.hset("player:data:" + event.getPlayer().getUniqueId(), "server", event.getServer().getInfo().getName());
                plugin.getRedis().write((new RedisMessage(plugin, Payload.SERVER_CHANGE)).setParam("uuid", event.getPlayer().getUniqueId().toString()).setParam("server", event.getServer().getInfo().getName()).setParam("proxy", plugin.getProxyName()).toJSON());
                return null;
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnect(ServerConnectEvent event){
        List<String> servers = new ArrayList<>(plugin.getConfig().getStringList("maintenance.servers"));
        String msg = this.plugin.getConfig().getString("maintenance.kick-msg");
        if (servers.contains(event.getTarget().getName())){
            if (entryCheck(event.getPlayer(), event.getTarget().getName())){
                event.setCancelled(true);
                plugin.getUtils().sendMessage(event.getPlayer(), msg);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLoginProcess(PreLoginEvent e) {
        String msg = this.plugin.getConfig().getString("maintenance.kick-msg");
        String noSlots = "&cLo sentimos, no hay cupo disponible para que entres al servidor.";
        if (plugin.getFileUtils().getWhitelist().getBoolean("settings.enabled")){
            plugin.getUtils().sendMessage(plugin.getProxy().getConsole(), "Whitelist enabled");
            boolean allowFill = plugin.getFileUtils().getWhitelist().getBoolean("settings.allow-fill");
            int limit = plugin.getFileUtils().getWhitelist().getInt("settings.limit");
            List<String> names = new ArrayList<>(plugin.getFileUtils().getWhitelist().getStringList("list"));

            if (whitelistCheck(e.getConnection().getName())){
                plugin.getUtils().sendMessage(plugin.getProxy().getConsole(), "Whitelist check passed for " + e.getConnection().getName());
                return;
            } else {
                if (!entryCheck(e.getConnection().getName())){
                    plugin.getUtils().sendMessage(plugin.getProxy().getConsole(), "Whitelist check granted for " + e.getConnection().getName());
                    return;
                }
                if (plugin.getConfig().getBoolean("maintenance.enabled")) {
                    if (entryCheck(e.getConnection().getName())) {
                        e.setCancelled(true);
                        e.setCancelReason(this.plugin.getUtils().getMSG(null, msg));
                        plugin.getUtils().sendMessage(plugin.getProxy().getConsole(), "Maintenance check failed for " + e.getConnection().getName());
                        return;
                    }
                }
                if (!allowFill){
                    plugin.getUtils().sendMessage(plugin.getProxy().getConsole(), "Whitelist check failed for " + e.getConnection().getName() + " (no fill)");
                    e.setCancelled(true);
                    noSlots = plugin.getUtils().ct(noSlots);
                    e.setCancelReason(TextComponent.fromLegacyText(noSlots));
                    return;
                }
                int online = plugin.getProxy().getPlayers().size();
                if (online >= limit){
                    e.setCancelled(true);
                    noSlots = plugin.getUtils().ct(noSlots);
                    e.setCancelReason(TextComponent.fromLegacyText(noSlots));
                    plugin.getUtils().sendMessage(plugin.getProxy().getConsole(), " Whitelist check failed for " + e.getConnection().getName() + " (no slots)");
                    return;
                }
            }
        }
        if (!plugin.getConfig().getBoolean("maintenance.enabled")) {
            return;
        }
        if (entryCheck(e.getConnection().getName())) {
            e.setCancelled(true);
            e.setCancelReason(this.plugin.getUtils().getMSG(null, msg));
            plugin.getUtils().sendMessage(plugin.getProxy().getConsole(), "Maintenance check failed for " + e.getConnection().getName());
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent e){
        ProxiedPlayer p = e.getPlayer();
        if (plugin.getFileUtils().getWhitelist().getBoolean("settings.enabled")){
            if (!plugin.isEnabledEvents()) return;
            List<String> names = new ArrayList<>(plugin.getFileUtils().getWhitelist().getStringList("list"));
            names = names.stream().map(String::toLowerCase).collect(Collectors.toList());

            if (whitelistCheck(p.getName())){
                names.remove(p.getName().toLowerCase());
                plugin.getFileUtils().getWhitelist().set("list", names);
                plugin.getFileUtils().saveWhitelist();
            }
        }
    }

    public boolean entryCheck(ProxiedPlayer p, String server) {
        String perm = "proxyutils.maintenance.bypass";
        List<String> list = this.plugin.getConfig().getStringList("maintenance.whitelist");
        if (!list.isEmpty() && list.contains(p.getName().toLowerCase())){
            return false;
        }
        if (server!=null){
            if (!p.hasPermission(perm+"."+server)){
                return true;
            }
        }
        return !p.hasPermission(perm) && (server == null || !p.hasPermission(perm + "." + server));
    }

    public boolean entryCheck(String p) {
        List<String> list = this.plugin.getConfig().getStringList("maintenance.whitelist");
        return !list.contains(p.toLowerCase());
    }

    public boolean whitelistCheck(String p) {
        List<String> list = new ArrayList<>(plugin.getFileUtils().getWhitelist().getStringList("list"));
        list = list.stream().map(String::toLowerCase).collect(Collectors.toList());
        return list.contains(p.toLowerCase());
    }

}
