package me.thejokerdev.frozzcore.listeners;

import com.cryptomorin.xseries.XSound;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.api.events.EconomyChangeEvent;
import me.thejokerdev.frozzcore.api.events.PlayerChangeLangEvent;
import me.thejokerdev.frozzcore.api.events.PlayerNickEvent;
import me.thejokerdev.frozzcore.enums.EconomyAction;
import me.thejokerdev.frozzcore.type.FUser;
import me.thejokerdev.frozzcore.type.NickData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class PluginListener implements Listener {
    private final SpigotMain plugin;

    public PluginListener(SpigotMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEconomyChangeEvent(EconomyChangeEvent event){
        FUser user = event.getUser();
        EconomyAction action = event.getAction();

        double coins = event.getAmount();
        //format into 0.0f format
        String money = String.format("%.1f", coins);

        if (action == EconomyAction.ADD){
            user.sendMSGWithObjets("%core_general_economy.add%", money);
            XSound.BLOCK_NOTE_BLOCK_PLING.play(user.getPlayer(), 1f, 2f);
        } else if (action == EconomyAction.REMOVE){
            user.sendMSGWithObjets("%core_general_economy.remove%", money);
            XSound.ENTITY_ENDERMAN_TELEPORT.play(user.getPlayer(), 1f, 0.5f);
        } else if (action == EconomyAction.SET){
            user.sendMSGWithObjets("%core_general_economy.set%", money);
            XSound.ENTITY_PLAYER_LEVELUP.play(user.getPlayer(), 1f, 1f);
        }
    }

    @EventHandler
    public void onPlayerChangeLangEvent(PlayerChangeLangEvent event){
        Player player = event.getPlayer();
        FUser user = plugin.getClassManager().getPlayerManager().getUser(player);
        user.getItemsManager().reloadItems();
        plugin.getClassManager().getMenusManager().loadMenus(player);
        user.sendMSGWithObjets("%core_general_language.changed%", event.getNewLang());
        XSound.BLOCK_NOTE_BLOCK_PLING.play(user.getPlayer(), 1f, 2f);
    }

    @EventHandler
    public void onPlayerNickEvent(PlayerNickEvent event){
        FUser user = event.getUser();
        PlayerNickEvent.Cause cause = event.getCause();
        NickData nickData = user.getNickData();
        if (cause == PlayerNickEvent.Cause.NICK){
            if (nickData.getSkin() == null){
                nickData.loadAndApplySkin();
            } else {
                nickData.applySkin();
            }
            XSound.BLOCK_NOTE_BLOCK_PLING.play(user.getPlayer(), 1f, 2f);
        } else if (cause == PlayerNickEvent.Cause.UNNICK){
            nickData.resetSkin();
            XSound.ENTITY_ENDERMAN_TELEPORT.play(user.getPlayer(), 1f, 0.5f);
        }
        new BukkitRunnable(){
            @Override
            public void run() {
                plugin.getClassManager().getMenusManager().loadMenus(user.getPlayer());
                user.getItemsManager().reloadItems();
            }
        }.runTaskLaterAsynchronously(plugin, 5L);
        user.saveData(false);
    }
}
