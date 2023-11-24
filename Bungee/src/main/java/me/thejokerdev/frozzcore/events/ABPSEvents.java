package me.thejokerdev.frozzcore.events;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.managers.Permissions;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Iterator;
import java.util.List;

public class ABPSEvents implements Listener {
    private final BungeeMain plugin;

    public ABPSEvents(BungeeMain plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onChatABPS(ChatEvent e){
        if (e.isCancelled()) {
            return;
        }
        if (!(e.getSender() instanceof ProxiedPlayer)) {
            return;
        }
        if (!e.isCommand()) {
            return;
        }
        if (!plugin.getConfig().getBoolean("abps.enabled")){
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer)e.getSender();
        String perm = Permissions.ABPS_BYPASS.get();
        if (player.hasPermission(perm)) {
            return;
        }
        String command = e.getMessage().split(" ")[0].toLowerCase();
        String fullCmd = e.getMessage();
        if (command.length() < 1) {
            return;
        }
        command = command.substring(1);
        fullCmd = fullCmd.substring(1);
        if (player.hasPermission(perm+ "." + command)) {
            return;
        }
        if (equalsIgnoreCase(getBlockedCmds(), command)) {
            e.setCancelled(true);
            plugin.getUtils().sendMessage(player, "abps.user");
            for (ProxiedPlayer online : plugin.getProxy().getPlayers()) {
                if (online.hasPermission(Permissions.ABPS_NOTIFY.get())) {
                    plugin.getUtils().sendMessage(online, plugin.getUtils().getMSG(player, plugin.getFileUtils().getMessages().getString("abps.admin").replace("{command}", fullCmd)).getText());
                }
            }

        }
    }

    public List<String> getBlockedCmds(){
        return plugin.getConfig().getStringList("abps.blocked-commands");
    }

    public boolean equalsIgnoreCase(List<String> list, String searchString) {
        if (list != null && searchString != null) {
            if (searchString.isEmpty()) {
                return true;
            } else {
                Iterator<String> var4 = list.iterator();

                String string;
                do {
                    if (!var4.hasNext()) {
                        return false;
                    }

                    string = var4.next();
                } while(string == null || !string.equalsIgnoreCase(searchString));

                return true;
            }
        } else {
            return false;
        }
    }

    @EventHandler(priority = 127)
    public void onPlayerTab(TabCompleteEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer)event.getSender();
        if (player.hasPermission("proxyutils.tab-complete.bypass")) {
            return;
        }
        if (!plugin.getConfig().getBoolean("abps.enabled")){
            return;
        }
        String perm = Permissions.ABPS_BYPASS.get();
        if (player.hasPermission(perm)) {
            return;
        }
        event.setCancelled(true);
    }

}
