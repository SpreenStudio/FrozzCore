package me.thejokerdev.frozzcore.api.utils;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class SkinUtils {
    public static String getSkinFromPlayer(OfflinePlayer player) {
        return SkullUtils.getValue(player.getName());
    }
}
