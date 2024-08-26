package me.thejokerdev.frozzcore.api.utils;

import com.cryptomorin.xseries.SkullUtils;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class SkinUtils {
    public static String getSkinFromPlayer(OfflinePlayer player) {
        ItemMeta itemMeta = XMaterial.PLAYER_HEAD.parseItem().getItemMeta();
        SkullMeta skullMeta = SkullUtils.applySkin(itemMeta, player);
        return SkullUtils.getSkinValue(skullMeta);
    }
}
