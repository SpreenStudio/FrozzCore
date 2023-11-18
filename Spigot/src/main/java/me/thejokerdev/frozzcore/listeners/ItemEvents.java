package me.thejokerdev.frozzcore.listeners;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.enums.Modules;
import me.thejokerdev.frozzcore.type.Button;
import me.thejokerdev.frozzcore.type.FUser;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class ItemEvents implements Listener {

    private final SpigotMain plugin;

    public ItemEvents(SpigotMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        FUser user = plugin.getClassManager().getPlayerManager().getUser(p);
        World w = p.getWorld();

        if (!plugin.getConfig().getBoolean("modules.items")){
            return;
        }

        if (p.getGameMode() != GameMode.CREATIVE) {
            if (plugin.getUtils().isWorldProtected(w, Modules.LOBBY)){
                e.setCancelled(true);
            }
        } else {
            if (!p.hasPermission("core.admin.build")){
                if (plugin.getUtils().isWorldProtected(w, Modules.LOBBY)){
                    e.setCancelled(true);
                }
            }
        }
        if (!plugin.getUtils().isWorldProtected(w, Modules.ITEMS)){
            return;
        }

        ItemStack item = e.getItem();
        if (item == null) {
            return;
        }
        if (p.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        for (Button b : user.getItemsManager().getItems().values()) {
            if (!b.canView()){
                continue;
            }
            if (b.getItem().isSimilar(p, item)) {
                if (item.getType() == Material.ENDER_PEARL){
                    e.setCancelled(true);
                }
                b.executePhysicallyItemsActions(e);
                if (b.canInteract()){
                    e.setCancelled(false);
                }
            }
        }
    }

    @EventHandler
    public void onInteractEvent(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        FUser user = plugin.getClassManager().getPlayerManager().getUser(p);

        if (!plugin.getConfig().getBoolean("modules.items")){
            return;
        }

        if (!plugin.getUtils().isWorldProtected(p.getWorld(), Modules.ITEMS)){
            return;
        }

        if (plugin.getConfig().getBoolean("settings.perWorld") && plugin.getSpawn() != null){
            if (!plugin.getSpawn().getWorld().equals(p.getWorld())){
                return;
            }
        }

        ItemStack item = e.getCurrentItem();
        if (item == null) {
            return;
        }
        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.ADVENTURE) {
            return;
        }
        for (Button b : user.getItemsManager().getItems().values()) {
            if (b.getItem().isSimilar(p, item)) {
                if (b.canInteract()){
                    e.setCancelled(false);
                    continue;
                }
                e.setCancelled(true);
            }
        }
        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.ADVENTURE) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e){
        if (!plugin.getConfig().getBoolean("modules.items")){
            return;
        }

        if (!plugin.getUtils().isWorldProtected(e.getEntity().getWorld(), Modules.ITEMS)){
            return;
        }

        Entity proj = e.getEntity();
        if (proj instanceof Arrow){
            if (((Arrow) proj).getShooter() instanceof Player){
                Player p = ((Player) ((Arrow) proj).getShooter()).getPlayer();
                if (p == null){
                    return;
                }
                ItemStack bow = p.getItemInHand();
                if (bow == null){
                    return;
                }
                for (Button b : plugin.getClassManager().getPlayerManager().getUser(p).getItemsManager().getItems().values()){
                    if (b.canInteract()){
                        if (b.hasMetaData() && b.getMetaData().equalsIgnoreCase("tpbow")){
                            proj.setCustomName("tpbow");
                            task((Arrow) proj);
                        }
                    }
                }
            }
        }
    }

    public void task(Arrow arrow){
        new BukkitRunnable() {
            int i = 500;
            @Override
            public void run() {
                if (arrow.isDead() || i <= 0){
                    cancel();
                    return;
                }
                if (arrow.isOnGround()){
                    cancel();
                    return;
                }
                if (arrow.getCustomName() == null){
                    return;
                }
                if (!arrow.getCustomName().equalsIgnoreCase("tpbow")){
                    return;
                }
                Location loc = arrow.getLocation();
                loc.getWorld().playEffect(loc, Effect.HAPPY_VILLAGER, 1);
                i--;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e){
        Entity proj = e.getEntity();
        if (proj instanceof Arrow){
            Arrow arrow = (Arrow) proj;
            if (arrow.getShooter() instanceof Player){
                Player p = ((Player) ((Arrow) proj).getShooter()).getPlayer();
                if (p == null){
                    return;
                }
                if (proj.getCustomName()!=null && proj.getCustomName().equals("tpbow")){
                    Location loc = proj.getLocation();
                    loc.setYaw(p.getLocation().getYaw());
                    loc.setPitch(p.getLocation().getPitch());
                    p.teleport(loc);
                    proj.remove();
                }
            }
        }
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent e){

        if (!plugin.getConfig().getBoolean("modules.items")){
            return;
        }

        if (!plugin.getUtils().isWorldProtected(e.getPlayer().getWorld(), Modules.ITEMS)){
            return;
        }
        if (plugin.getConfig().getBoolean("settings.perWorld") && plugin.getSpawn() != null){
            if (!plugin.getSpawn().getWorld().equals(e.getPlayer().getWorld())){
                return;
            }
        }
        if (e.getNewGameMode() != GameMode.CREATIVE) {
            FUser player = plugin.getClassManager().getPlayerManager().getUser(e.getPlayer());
            player.getItemsManager().reloadItems();
        }
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent e) {


        if (!plugin.getUtils().isWorldProtected(e.getPlayer().getWorld(), Modules.LOBBY)){
            return;
        }

        if (!plugin.getConfig().getBoolean("modules.items")){
            return;
        }
        if (plugin.getConfig().getBoolean("settings.perWorld") && plugin.getSpawn() != null){
            if (!plugin.getSpawn().getWorld().equals(e.getPlayer().getWorld())){
                return;
            }
        }

        FUser user = plugin.getClassManager().getPlayerManager().getUser(e.getPlayer());

        ItemStack item = e.getItem().getItemStack();
        if (item == null) {
            return;
        }
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        for (Button b : user.getItemsManager().getItems().values()) {
            if (b.getItem().isSimilar(e.getPlayer(), item)) {
                e.setCancelled(true);
            }
        }
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        if (!plugin.getConfig().getBoolean("modules.items")){
            return;
        }
        if (!plugin.getUtils().isWorldProtected(e.getPlayer().getWorld(), Modules.LOBBY)){
            return;
        }
        if (plugin.getConfig().getBoolean("settings.perWorld") && plugin.getSpawn() != null){
            if (!plugin.getSpawn().getWorld().equals(e.getPlayer().getWorld())){
                return;
            }
        }
        FUser user = plugin.getClassManager().getPlayerManager().getUser(e.getPlayer());

        ItemStack item = e.getItemDrop().getItemStack();
        if (item == null) {
            return;
        }
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        for (Button b : user.getItemsManager().getItems().values()) {
            if (b.getItem().isSimilar(e.getPlayer(), item)) {
                e.setCancelled(true);
            }
        }
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        e.setCancelled(true);
    }
}
