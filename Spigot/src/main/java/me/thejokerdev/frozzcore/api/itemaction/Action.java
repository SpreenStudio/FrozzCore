package me.thejokerdev.frozzcore.api.itemaction;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.enums.ModifierStatus;
import me.thejokerdev.frozzcore.type.FUser;
import me.thejokerdev.frozzcore.type.ItemActionExecutor;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;

import java.util.Collections;

public class Action implements ItemActionExecutor {

    private static final SpigotMain plugin = SpigotMain.getPlugin();

    @Override
    public boolean onCommand(Player player, String label) {

        FUser user = plugin.getClassManager().getPlayerManager().getUser(player);

        String s = label.replace("[action]", "");
        if (s.equalsIgnoreCase("visibility")){
            plugin.utils.changeVisibility(player);
            user.saveData(false);
        }
        if (s.equalsIgnoreCase("return")){
            return false;
        }
        if (s.equalsIgnoreCase("jump")){
            user.setJump(user.getJump() == ModifierStatus.OFF ? ModifierStatus.ON : ModifierStatus.OFF);
            user.saveData(false);
        }
        if (s.equalsIgnoreCase("disableJump")){
            user.setJump(ModifierStatus.DEACTIVATED);
            user.saveData(false);
        }
        if (s.equalsIgnoreCase("doublejump")){
            user.setDoubleJump(user.getDoubleJump() == ModifierStatus.OFF ? ModifierStatus.ON : ModifierStatus.OFF);
            user.saveData(false);
        }
        if (s.equalsIgnoreCase("disableDoubleJump")){
            user.setDoubleJump(ModifierStatus.DEACTIVATED);
            user.saveData(false);
        }
        if (s.equalsIgnoreCase("fly")){
            user.setAllowFlight(user.getAllowFlight() == ModifierStatus.OFF ? ModifierStatus.ON : ModifierStatus.OFF);
            user.saveData(false);
        }
        if (s.equalsIgnoreCase("disableFly")){
            user.setAllowFlight(ModifierStatus.DEACTIVATED);
            user.saveData(false);
        }
        if (s.equalsIgnoreCase("disableSpeed")){
            user.setSpeed(ModifierStatus.DEACTIVATED);
            user.saveData(false);
        }
        if (s.equalsIgnoreCase("enderbutt")){
            if (player.getVehicle() != null && player.getVehicle() instanceof EnderPearl){
                EnderPearl pearl = (EnderPearl) player.getVehicle();
                pearl.remove();
            }
            EnderPearl pearl = player.launchProjectile(EnderPearl.class);
            pearl.setPassenger(player);
            plugin.utils.actions(player, Collections.singletonList("[sound]ENTITY_ENDERMAN_TELEPORT,1.0,1.0"));
            plugin.utils.setupEnderpearlRunnable(pearl);
        }
        return false;
    }
}
