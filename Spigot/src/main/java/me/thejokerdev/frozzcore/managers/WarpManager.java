package me.thejokerdev.frozzcore.managers;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.api.utils.FileUtils;
import me.thejokerdev.frozzcore.type.Warp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WarpManager {
    public static WarpManager INSTANCE;
    private final SpigotMain plugin;
    public ConcurrentHashMap<String, Warp> warpMap;

    public WarpManager(SpigotMain plugin) {
        this.plugin = plugin;
        INSTANCE = this;
    }

    public Warp getWarp(String name) {
        return warpMap.get(name);
    }

    public List<Warp> getWarps() {
        return new ArrayList<>(warpMap.values());
    }

    public void load() {
        warpMap = new ConcurrentHashMap<>();

        plugin.console("Loading warps...");

        FileUtils warps = new FileUtils(plugin.getDataFolder(), "warps.yml");
        if (warps.get("warps") != null) {
            for (String key : warps.getSection("warps").getKeys(false)) {
                Warp warp = Warp.fromString(key);
                if (warp == null) {
                    plugin.console("{prefix}Error loading warp: " + key);
                    continue;
                }
                warpMap.put(key, warp);
            }
        }

        plugin.console(warpMap.size() + " warps loaded.");
    }

}
