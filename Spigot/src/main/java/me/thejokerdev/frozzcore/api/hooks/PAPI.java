package me.thejokerdev.frozzcore.api.hooks;

import cloud.timo.TimoCloud.api.objects.ServerObject;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.api.visual.Animation;
import me.thejokerdev.frozzcore.api.visual.Color;
import me.thejokerdev.frozzcore.type.FUser;
import me.thejokerdev.frozzcore.type.Lang;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PAPI extends PlaceholderExpansion {
    private SpigotMain plugin;

    public PAPI(SpigotMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "core";
    }

    @Override
    public @NotNull String getAuthor() {
        return "TheJokerDev";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean register() {
        return super.register();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (plugin.getConfig().get("placeholders")!=null){
            for (String key : plugin.getConfig().getConfigurationSection("placeholders").getKeys(false)){
                if (key.equalsIgnoreCase(params)){
                    return plugin.getClassManager().getUtils().formatMSG(player, plugin.getConfig().getString("placeholders."+key));
                }
            }
        }
        if (params.equals("chatcolor")){
            LinkedList<String> list = new LinkedList<>(plugin.getConfig().getConfigurationSection("chat.colors").getKeys(false));
            String out = plugin.getConfig().getString("chat.colors.default");
            for (String perm : list){
                if (perm.equals("default") && !player.hasPermission("core.chatcolor.status")){
                    continue;
                }
                if (player.hasPermission("core.chatcolor."+perm)){
                    out = plugin.getConfig().getString("chat.colors."+perm);
                }
            }
            return out;
        }
        if (params.equals("fly")){
            FUser user = plugin.getClassManager().getPlayerManager().getUser(player);
            if (user != null){
                return user.getAllowFlight().name();
            }
        }
        if (params.equals("speed")){
            FUser user = plugin.getClassManager().getPlayerManager().getUser(player);
            if (user != null){
                return user.getSpeed().name();
            }
        }
        if (params.equals("server-id")) {
            if (plugin.getServerManager() != null) {
                ServerObject server = plugin.getServerManager().getActualServer();
                String str = server.getName();
                int i = 1;
                try {
                    String[] split = str.split("-");
                    i = Integer.parseInt(split[split.length - 1]);
                } catch (NumberFormatException ignored) {
                }
                return String.valueOf(i);
            }
            return plugin.getServerName();
        }
        if (params.equals("jump")){
            FUser user = plugin.getClassManager().getPlayerManager().getUser(player);
            if (user != null){
                return user.getJump().name();
            }
        }
        if (params.equals("doublejump")){
            FUser user = plugin.getClassManager().getPlayerManager().getUser(player);
            if (user != null){
                return user.getDoubleJump().name();
            }
        }
        if (params.equals("hype")){
            if (player != null){
                FUser user = plugin.getClassManager().getPlayerManager().getUser(player);
                if (user != null){
                    return user.getHype()+"";
                }
            }
        }
        if (params.equals("visibility")){
            if (player != null){
                FUser user = plugin.getClassManager().getPlayerManager().getUser(player);
                if (user != null){
                    return plugin.getClassManager().getUtils().getLangMSG(player, "visibility."+user.getVisibilityType().name());
                }
            }
        }
        if (params.equals("visibility_type")){
            if (player != null){
                FUser user = plugin.getClassManager().getPlayerManager().getUser(player);
                if (user != null){
                    return user.getVisibilityType().name();
                }
            }
        }
        String[] split = params.split("_");
        if (split.length == 2){
            String key = split[0];
            String group = split[1];
            if (plugin.getServerManager() == null){
                return PlaceholderAPI.setPlaceholders(player, plugin.getClassManager().getUtils().getMSG("error"));
            }

            boolean multiple = group.contains(",");
            String str = plugin.getClassManager().getUtils().getLangMSG(player, "lobby@general.offline");
            str =  plugin.getClassManager().getUtils().formatMSG(player, str == null ? "&cOffline" : str);

            if (key.equalsIgnoreCase("online")){
                if (multiple){
                    String[] groups = group.split(",");
                    int amount = 0;
                    for (String g : groups){
                        if (plugin.getServerManager().getGroup(g) == null){
                            return str;
                        }
                        amount += plugin.getServerManager().getGroupOnlineAmount(g);
                    }
                    return amount+"";
                }
                if (plugin.getServerManager().getGroup(group) == null){
                    return str;
                }
                return plugin.getServerManager().getGroupOnlineAmount(group)+"";
            }

            if (key.equalsIgnoreCase("max")){
                if (multiple){
                    String[] groups = group.split(",");
                    int amount = 0;
                    for (String g : groups){
                        if (plugin.getServerManager().getGroup(g) == null){
                            return str;
                        }
                        amount += plugin.getServerManager().getGroupMaxPlayerAmount(g);
                    }
                    return amount+"";
                }
                if (plugin.getServerManager().getGroup(group) == null){
                    return str;
                }
                return plugin.getServerManager().getGroupMaxPlayerAmount(group)+"";
            }

            if (key.equalsIgnoreCase("status")){
                if (multiple){
                    String[] groups = group.split(",");
                    List<String> list = new ArrayList<>();
                    for (String g : groups){
                        if (plugin.getServerManager().getGroup(g) == null){
                            return str;
                        }
                        list.add(plugin.getServerManager().getTranslatedStatus(player != null ? plugin.getClassManager().getPlayerManager().getUser(player) : null, g));
                    }
                    return String.join(", ", list);
                }
                if (plugin.getServerManager().getGroup(group) == null){
                    return str;
                }
                return plugin.getServerManager().getTranslatedStatus(player != null ? plugin.getClassManager().getPlayerManager().getUser(player) : null, group);
            }

            if (key.equalsIgnoreCase("bar")){
                boolean offline = false;
                int online = 0;
                int max = 0;
                if (multiple){
                    String[] groups = group.split(",");
                    for (String g : groups){
                        if (!offline && plugin.getServerManager().getGroup(g) == null){
                            offline = true;
                            continue;
                        }
                        online += plugin.getServerManager().getGroupOnlineAmount(g);
                        max += plugin.getServerManager().getGroupMaxPlayerAmount(g);
                    }
                }else{
                    if (plugin.getServerManager().getGroup(group) != null){
                        offline = true;
                    }
                }

                String defaultColor = "&7";
                String onlineColor = "&b";
                String symbol = "■";

                if (offline){
                    return str;
                }

                double percent = 0;
                if (online > 0 && max > 0){
                    percent = (double) online / (double) max;
                }
                StringBuilder out = new StringBuilder();
                for (int i = 0; i < 10; i++){
                    if (i < percent * 10){
                        out.append(plugin.getClassManager().getUtils().formatMSG(player, onlineColor+symbol));
                    }else{
                        out.append(plugin.getClassManager().getUtils().formatMSG(player, defaultColor+symbol));
                    }
                }
                return out.toString();
            }
        }
        if (split.length == 1 || split.length >= 4) {
            String arg1 = split[0];
            if (arg1.equalsIgnoreCase("wave")) {
                if (split.length > 3){
                    String arg2 = split[1];
                    String arg3 = split[2];
                    String arg4 = split[3];
                    if (split.length == 5){
                        String arg5 = split[4].toUpperCase();
                        return Animation.wave(arg2, Boolean.parseBoolean(arg5), 5, 10, Color.from(arg3), Color.from(arg4));
                    }
                    return Animation.wave(arg2, Color.from(arg3), Color.from(arg4));
                }
                return Animation.wave("Test", Color.from("986532"), Color.from("722626"));
            }
            if (arg1.equalsIgnoreCase("fading")) {
                if (split.length >= 4) {
                    String arg2 = split[1];
                    String arg3 = split[2];
                    String arg4 = split[3];
                    if (split.length == 5){
                        String arg5 = split[4].toUpperCase();
                        return Animation.fading(arg2, Boolean.parseBoolean(arg5), 5, 10, Color.from(arg3), Color.from(arg4));
                    }
                    return Animation.fading(arg2, Color.from(arg3), Color.from(arg4));
                }
                return Animation.fading("Test", Color.from("986532"), Color.from("722626t"));
            }
        }
        if (!params.contains("_")){
            return PlaceholderAPI.setPlaceholders(player, plugin.getClassManager().getUtils().getMSG("error"));
        }
        if (split.length != 2){
            return PlaceholderAPI.setPlaceholders(player, plugin.getClassManager().getUtils().getMSG("error"));
        }
        String lang = plugin.getClassManager().getLangManager().getDefault();
        if (player != null) {
            FUser fUser = plugin.getClassManager().getPlayerManager().getUser(player);
            if (fUser != null) {
                lang = fUser.getLang();
            }
        }
        String section = split[0];

        if (!plugin.getClassManager().getLangManager().getLanguages().containsKey(section)){
            return PlaceholderAPI.setPlaceholders(player, plugin.getClassManager().getUtils().getMSG("sectionNotFound").replace("{section}", section));
        }

        if (plugin.getClassManager().getLangManager().getLanguageOfSection(section, lang) == null){
            lang = plugin.getClassManager().getLangManager().getDefault();
        }

        Lang language = plugin.getClassManager().getLangManager().getLanguageOfSection(section, lang);
        String key = split[1];

        if (language.getFile().get(key)==null){
            language = plugin.getClassManager().getLangManager().getLanguageOfSection(section, plugin.getClassManager().getLangManager().getDefault());
        }

        if (language.getFile().get(key)==null) {
            return PlaceholderAPI.setPlaceholders(player, plugin.getClassManager().getUtils().getMSG("keyNotFound").replace("{key}", key));
        }

        return plugin.getUtils().formatMSG(player, language.getFile().getString(key));
    }
}