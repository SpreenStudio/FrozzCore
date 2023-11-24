package me.thejokerdev.frozzcore.events;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.managers.Managers;
import me.thejokerdev.frozzcore.managers.Permissions;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatEvents implements Listener {
    private final BungeeMain plugin;
    
    public ChatEvents(BungeeMain plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(ChatEvent e){
        if (!(e.getSender() instanceof ProxiedPlayer)){
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) e.getSender();

        if (!p.hasPermission(Permissions.STAFFCHAT_STAFF.get())){
            return;
        }
        String msg = e.getMessage();
        if (msg.startsWith("/")){
            return;
        }
        String format = plugin.getConfig().getString("staffchat.format");
        format = plugin.getUtils().getMSG(p, format).getText();

        boolean b = plugin.getConfig().getBoolean("staffchat.symbol.enabled");
        if (b){
            String prefix = plugin.getConfig().getString("staffchat.symbol.id");
            if (e.getMessage().toLowerCase().startsWith(prefix)){

                msg = msg.replaceFirst(prefix, "");
                msg = format+msg;

                plugin.getUtils().sendMSGtoStaff(p, msg);
                e.setCancelled(true);
                return;
            }
        }

        if (Managers.isToggled(p)){
            e.setCancelled(true);
            msg = format+msg;
            plugin.getUtils().sendMSGtoStaff(p, msg);
        }
    }
}
