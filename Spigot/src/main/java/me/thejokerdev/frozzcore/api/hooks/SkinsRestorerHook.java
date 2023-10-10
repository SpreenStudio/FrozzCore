package me.thejokerdev.frozzcore.api.hooks;

import me.thejokerdev.frozzcore.SpigotMain;
import net.skinsrestorer.api.SkinsRestorerAPI;

import java.util.UUID;

public class SkinsRestorerHook {
    private final SpigotMain plugin;

    public SkinsRestorerHook(SpigotMain plugin) {
        this.plugin = plugin;
    }

    public String getSkin(UUID uuid){
        return SkinsRestorerAPI.getApi().getProfile(uuid.toString()).getValue();
    }

}
