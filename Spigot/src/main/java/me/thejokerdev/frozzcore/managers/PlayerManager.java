package me.thejokerdev.frozzcore.managers;

import lombok.Getter;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.type.FUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class PlayerManager implements Listener {
    private final ConcurrentHashMap<UUID, FUser> users;
    private final SpigotMain plugin;

    public PlayerManager(SpigotMain plugin) {
        this.plugin = plugin;
        users = new ConcurrentHashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public FUser getUser(Player p){
        return users.computeIfAbsent(p.getUniqueId(), k -> registerUser(p));
    }

    public FUser registerUser(Player p){
        FUser user = new FUser(p);
        user.initItems();
        return user;
    }

    public FUser removeUser(Player p){
        if (users.containsKey(p.getUniqueId())){
            users.get(p.getUniqueId()).saveData(true);
        }
        return users.remove(p.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent e){
        getUser(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        plugin.getClassManager().getMenusManager().getPlayerMenus(p).values().forEach(menu -> {
            if (menu.getTask() != null) {
                menu.getTask().cancel();
            }
        });
        FUser user = removeUser(p);
        if (user.isNicked()){
            user.getNickData().resetSkin();
        }
    }
}
