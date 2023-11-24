package me.thejokerdev.frozzcore.hooks.cloud;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.hooks.webhooks.WebHook;
import studio.spreen.cloud.api.CloudAPI;
import studio.spreen.cloud.api.events.EventHandler;
import studio.spreen.cloud.api.events.Listener;
import studio.spreen.cloud.api.events.base.BaseConnectEvent;
import studio.spreen.cloud.api.events.base.BaseDisconnectEvent;
import studio.spreen.cloud.api.events.proxy.ProxyRegisterEvent;
import studio.spreen.cloud.api.events.proxy.ProxyUnregisterEvent;
import studio.spreen.cloud.api.events.proxyGroup.ProxyGroupCreatedEvent;
import studio.spreen.cloud.api.events.proxyGroup.ProxyGroupDeletedEvent;
import studio.spreen.cloud.api.events.server.ServerRegisterEvent;
import studio.spreen.cloud.api.events.serverGroup.ServerGroupCreatedEvent;
import studio.spreen.cloud.api.events.serverGroup.ServerGroupDeletedEvent;

import java.time.Instant;

public class CloudEvents implements Listener {
    private final BungeeMain plugin;

    public CloudEvents(BungeeMain plugin) {
        this.plugin = plugin;
        CloudAPI.getEventAPI().registerListener(this);
    }

    public WebHook sendMessage(){
        Instant timestamp = Instant.now();
        String time = "<t:%s:f>";
        String timeFormatted = String.format(time, timestamp.getEpochSecond());
        return plugin.getWebhookManager().getCloud().setFooter("Cloud").addField("Time", timeFormatted, false);
    }

    @EventHandler
    public void onBaseStart(BaseConnectEvent event) {
        sendMessage().setColor("#00ff00")
                .setTitle("New base connected")
                .addField("Base", event.getBase().getName(), true)
                .execute();
    }

    @EventHandler
    public void onBaseStop(BaseDisconnectEvent event) {
        sendMessage().setColor("#ff0000")
                .setTitle("Base disconnected")
                .addField("Base", event.getBase().getName(), true)
                .execute();
    }

    @EventHandler
    public void onProxyConnect(ProxyRegisterEvent event){
        sendMessage().setColor("#00ff00")
                .setTitle("New proxy connected")
                .addField("Proxy", event.getProxy().getName(), true)
                .execute();
    }

    @EventHandler
    public void onProxyDisconnect(ProxyUnregisterEvent event){
        sendMessage().setColor("#ff0000")
                .setTitle("Proxy disconnected")
                .addField("Proxy", event.getProxy().getName(), true)
                .execute();
    }

    @EventHandler
    public void onServerRegister(ServerRegisterEvent event){
        sendMessage().setColor("#00ff00")
                .setTitle("New server registered")
                .addField("Server", event.getServer().getName(), true)
                .execute();
    }

    @EventHandler
    public void onServerUnregister(ServerRegisterEvent event){
        sendMessage().setColor("#ff0000")
                .setTitle("Server unregistered")
                .addField("Server", event.getServer().getName(), true)
                .execute();
    }

    @EventHandler
    public void onServerGroupCreated(ServerGroupCreatedEvent event){
        sendMessage().setColor("#00ff00")
                .setTitle("New server group created")
                .addField("Server group", event.getServerGroup().getName(), true)
                .execute();
    }

    @EventHandler
    public void onServerGroupDeleted(ServerGroupDeletedEvent event){
        sendMessage().setColor("#ff0000")
                .setTitle("Server group deleted")
                .addField("Server group", event.getServerGroup().getName(), true)
                .execute();
    }

    @EventHandler
    public void onProxyGroupCreated(ProxyGroupCreatedEvent event){
        sendMessage().setColor("#00ff00")
                .setTitle("New proxy group created")
                .addField("Proxy group", event.getProxyGroup().getName(), true)
                .execute();
    }

    @EventHandler
    public void onProxyGroupDeleted(ProxyGroupDeletedEvent event){
        sendMessage().setColor("#ff0000")
                .setTitle("Proxy group deleted")
                .addField("Proxy group", event.getProxyGroup().getName(), true)
                .execute();
    }

    public void unregister(){
        CloudAPI.getEventAPI().unregisterListener(this);
    }
}
