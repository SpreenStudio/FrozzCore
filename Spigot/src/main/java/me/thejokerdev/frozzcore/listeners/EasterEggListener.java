package me.thejokerdev.frozzcore.listeners;

import com.cryptomorin.xseries.XMaterial;
import me.thejokerdev.frozzcore.SpigotMain;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class EasterEggListener implements Listener {

    private final SpigotMain plugin;

    public EasterEggListener(SpigotMain plugin){
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onSkullInteract(PlayerInteractEvent event){
        if(event.getClickedBlock() == null)
            return;

        Block block = event.getClickedBlock();

        if(!block.getType().equals(XMaterial.PLAYER_HEAD.parseMaterial()))
            return;

        event.setCancelled(true);
        plugin.getClassManager().getEasterEggManager().addEaster(event.getPlayer(), block.getLocation());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event){
        plugin.getClassManager().getEasterEggManager().onLoadChunk(event.getChunk());
    }
}
