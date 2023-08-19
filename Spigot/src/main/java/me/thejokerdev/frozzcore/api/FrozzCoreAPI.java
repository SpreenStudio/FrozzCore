package me.thejokerdev.frozzcore.api;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.api.utils.FileUtils;
import me.thejokerdev.frozzcore.type.FUser;
import org.bukkit.entity.Player;

public class FrozzCoreAPI {
    @Getter
    private static final SpigotMain plugin = SpigotMain.getPlugin();

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
        FileUtils file = plugin.getClassManager().getLangManager().getLanguageOfSection(section, getLang(p)).getFile();
        if (file.get(key)==null) {
            String msg = plugin.getClassManager().getUtils().getMSG("keyNotFound").replace("{key}", key);
            return plugin.getUtils().formatMSG(p, msg);
        }
        return plugin.getUtils().formatMSG(p, file.getString(key));
    }
}
