package me.thejokerdev.frozzcore.listeners;

import com.cryptomorin.xseries.XSound;
import me.clip.placeholderapi.PlaceholderAPI;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.api.utils.Utils;
import me.thejokerdev.frozzcore.enums.Modules;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ChatListener implements Listener {
    private final SpigotMain plugin;

    public ChatListener(SpigotMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (event.isCancelled()) return;
        if (!plugin.getUtils().isWorldProtected(player.getWorld(), Modules.CHAT)) return;

        // Placeholder for color (of message) of the player
        String message = plugin.getConfig().getString("chat.format.message");
        message = message.replace("{color}", Utils.ct(plugin.getUtils().getChatColor(event.getPlayer()))) + event.getMessage();

        // FORMAT = PREFIX + NAME + SUFFIX
        String format = getChatFormat("prefix") +
                getChatFormat("name") +
                getChatFormat("suffix");

        // Set placeholders + parse chat color
        format = PlaceholderAPI.setPlaceholders(player, format);
        format = Utils.ct(format);

        // If permissions parse message with chat color
        if (player.hasPermission("core.colorchat")){
            message = Utils.ct(message);
        }

        // Add to format the message
        format += message;

        event.setFormat(format.replace("%", "%%"));

        // Validate receivers if per world setting is enabled
        if (isPerWorldSettingEnabled()) {
            event.getRecipients().clear();
            event.getRecipients().addAll(getRecipients(player.getWorld()));
        }

        publishToRedis(event);
    }

    public List<Player> getRecipients(World world) {
        return world.getPlayers();
    }

    private void publishToRedis(AsyncPlayerChatEvent e) {
        if (plugin.getRedis() != null && plugin.getClassManager().getLinkedChatManager() != null)
            plugin.getClassManager().getLinkedChatManager().sendMessage(e.getPlayer(), e.getFormat(), e.getMessage());
    }

    private boolean isPerWorldSettingEnabled() {
        return plugin.getConfig().getBoolean("settings.perWorld");
    }

    private String getChatFormat(String value) {
        return plugin.getConfig().getString("chat.format." + value);
    }

}
