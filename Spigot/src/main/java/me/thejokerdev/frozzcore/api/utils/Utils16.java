package me.thejokerdev.frozzcore.api.utils;

import me.thejokerdev.frozzcore.SpigotMain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils16 {
    private final SpigotMain plugin;

    public Utils16(SpigotMain plugin){
        this.plugin = plugin;
    }

    public static String colorize(String string) {
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        for (Matcher matcher = pattern.matcher(string); matcher.find(); matcher = pattern.matcher(string)) {
            String color = string.substring(matcher.start(), matcher.end());
            string = string.replace(color, ChatColor.of(color.replace("&", "")) + ""); // You're missing this replacing
        }
        string = org.bukkit.ChatColor.translateAlternateColorCodes('&', string); // Translates any & codes too
        return string;
    }

}
