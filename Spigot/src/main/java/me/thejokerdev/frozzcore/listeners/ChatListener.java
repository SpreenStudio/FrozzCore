package me.thejokerdev.frozzcore.listeners;

import com.cryptomorin.xseries.XSound;
import me.clip.placeholderapi.PlaceholderAPI;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.api.utils.Utils;
import me.thejokerdev.frozzcore.enums.Modules;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.LinkedList;

public class ChatListener implements Listener {
    private final SpigotMain plugin;

    public ChatListener(SpigotMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncPlayerChatEvent e){
        Player p = e.getPlayer();

        if (e.isCancelled()){
            return;
        }

        if (!plugin.getUtils().isWorldProtected(p.getWorld(), Modules.CHAT)){
            return;
        }

        String prefix = plugin.getConfig().getString("chat.format.prefix");
        String name = plugin.getConfig().getString("chat.format.name");
        String suffix = plugin.getConfig().getString("chat.format.suffix");
        String message = plugin.getConfig().getString("chat.format.message");
        message = message.replace("{color}", Utils.ct(plugin.getUtils().getChatColor(e.getPlayer())))+e.getMessage();

        String format = prefix+name+suffix;
        format = PlaceholderAPI.setPlaceholders(p, format);
        format = Utils.ct(format);
        format += message;
        if (p.hasPermission("core.colorchat")){
            format = Utils.ct(format);
        }


        e.setFormat(format.replace("%", "%%"));

        if (plugin.getConfig().getBoolean("settings.perWorld")){
            for (Player var5 : Bukkit.getServer().getOnlinePlayers()) {
                if (var5.getWorld() != p.getWorld()) {
                    e.getRecipients().remove(var5);
                }
            }
        }

        if(plugin.getRedis() != null && plugin.getClassManager().getLinkedChatManager() != null)
            plugin.getClassManager().getLinkedChatManager().sendMessage(e.getPlayer(), e.getFormat(), e.getMessage());
    }
}
