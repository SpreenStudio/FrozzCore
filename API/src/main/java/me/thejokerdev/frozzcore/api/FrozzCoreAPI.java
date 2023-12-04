package me.thejokerdev.frozzcore.api;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.api.utils.FileUtils;
import me.thejokerdev.frozzcore.type.FUser;
import me.thejokerdev.frozzcore.type.Lang;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class FrozzCoreAPI {
    private static SpigotMain plugin;

    public static SpigotMain getPlugin() {
        if (plugin==null) {
            plugin = SpigotMain.getPlugin();
        }
        return plugin;
    }

    public FrozzCoreAPI() {
        plugin = SpigotMain.getPlugin();
    }

    public static String getPrefix() {
        return plugin.getPrefix();
    }

    public FUser getUser(Player p) {
        return plugin.getClassManager().getPlayerManager().getUser(p);
    }

    public String getLang(Player p) {
        return plugin.getClassManager().getPlayerManager().getUser(p).getLang();
    }

    public String getTranslation(Player p, String section, String key){
        FileUtils file = getLangFile(getLang(p), section);
        if (file.get(key)==null) {
            String msg = plugin.getClassManager().getUtils().getMSG("keyNotFound").replace("{key}", key);
            return plugin.getUtils().formatMSG(p, msg);
        }
        return plugin.getUtils().formatMSG(p, file.getString(key));
    }

    public FileUtils getLangFile(String lang, String section) {
        return plugin.getClassManager().getLangManager().getLanguageOfSection(section, lang).getFile();
    }

    public String getTranslation(String lang, String section, String key){
        FileUtils file = getLangFile(lang, section);
        if (file.get(key)==null) {
            String msg = plugin.getClassManager().getUtils().getMSG("keyNotFound").replace("{key}", key);
            return plugin.getUtils().formatMSG(null, msg);
        }
        return plugin.getUtils().formatMSG(null, file.getString(key));
    }

    public void broadcast(Collection<Player> players, String section, String key, Object... placeholders) {
        List<Lang> files = plugin.getClassManager().getLangManager().getSection(section);
        HashMap<String, String> map = new HashMap<>();
        for (Lang file : files) {
            String msg = file.getFile().getString(key);
            if (msg==null) {
                msg = plugin.getClassManager().getUtils().getMSG("keyNotFound").replace("{key}", key);
            }
            map.put(file.getId(), msg);
        }
        for (Player p : players) {
            FUser user = getUser(p);
            String msg = map.get(user.getLang());
            if (msg==null) {
                continue;
            }
            user.sendMSGWithObjets(msg, placeholders);
        }
    }

    public void sendMSG(Player player, String section, String key, Object... objects){
        FUser user = getUser(player);
        user.sendMSGWithObjets(getTranslation(user.getPlayer(), section, key), objects);
    }

    public void sendMSG(Player player, String section, String key){
        FUser user = getUser(player);
        user.sendMSGWithObjets(getTranslation(user.getPlayer(), section, key));
    }
}
