package me.thejokerdev.frozzcore.type;

import lombok.Getter;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.api.utils.FileUtils;
import me.thejokerdev.frozzcore.api.utils.LocationUtil;
import me.thejokerdev.frozzcore.managers.WarpManager;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

@Getter
public class Warp {
    private final String name;
    private final Location location;
    private final String permission;
    private final int cost;

    public Warp(String name, Location location, String permission, int cost) {
        this.name = name;
        this.location = location;
        this.permission = permission;
        this.cost = cost;
    }

    public void teleport(Entity entity) {
        entity.teleport(location);
    }

    public static class Builder {
        private String name;
        private Location location;
        private String permission;
        private int cost;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder location(Location location) {
            this.location = location;
            return this;
        }

        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder cost(int cost) {
            this.cost = cost;
            return this;
        }

        public Warp build() {
            return new Warp(name, location, permission, cost);
        }
    }

    public void save() {
        FileUtils config = new FileUtils(SpigotMain.getPlugin().getDataFolder(), "warps.yml");
        ConfigurationSection section = config.createSection("warps." + name);
        section.set("location", LocationUtil.getString(location, true));
        section.set("permission", permission == null ? "" : permission);
        section.set("cost", cost);
        config.save();
        WarpManager.INSTANCE.warpMap.put(name, this);
    }

    public void delete() {
        FileUtils config = new FileUtils(SpigotMain.getPlugin().getDataFolder(), "warps.yml");
        config.set("warps." + name, null);
        config.save();
        WarpManager.INSTANCE.warpMap.remove(name);
    }

    public String toString() {
        return "Warp{" +
                "name='" + name + '\'' +
                ", location=" + LocationUtil.getString(location, true) +
                ", permission='" + permission + '\'' +
                ", cost=" + cost +
                '}';
    }

    public static Warp fromString(String string) {
        FileUtils warp = new FileUtils(SpigotMain.getPlugin().getDataFolder(), "warps.yml");
        ConfigurationSection section = warp.getSection("warps." + string);
        if (section == null) {
            return null;
        }

        Location location = LocationUtil.getLocation(section.getString("location"));
        String permission = section.getString("permission", "");
        int cost = section.getInt("cost",  0);

        return new Warp(string, location, permission, cost);
    }
}
