package me.thejokerdev.frozzcore;

import lombok.Getter;
import lombok.Setter;
import me.thejokerdev.frozzcore.api.cache.ItemsCache;
import me.thejokerdev.frozzcore.api.hooks.LP;
import me.thejokerdev.frozzcore.api.hooks.PAPI;
import me.thejokerdev.frozzcore.api.hooks.SR;
import me.thejokerdev.frozzcore.api.utils.FileUtils;
import me.thejokerdev.frozzcore.api.utils.LocationUtil;
import me.thejokerdev.frozzcore.api.utils.PluginMessageManager;
import me.thejokerdev.frozzcore.api.utils.Utils;
import me.thejokerdev.frozzcore.managers.ClassManager;
import me.thejokerdev.frozzcore.managers.ServerManager;
import me.thejokerdev.frozzcore.redis.Redis;
import me.thejokerdev.frozzcore.type.FUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

@Getter
@Setter
public final class SpigotMain extends JavaPlugin {
    private static SpigotMain plugin;
    private ClassManager classManager;
    private ItemsCache itemsCache;
    private PluginMessageManager pluginMessageManager;

    private LP lp = null;
    private SR sr = null;
    private Location spawn = null;
    public Utils utils;

    private boolean loaded = false;

    //Messaging
    private Redis redis;

    private String id;
    private String serverName;
    int tries = 0;

    private ServerManager serverManager;

    @Override
    public void onEnable() {
        /*if (!new AdvancedLicense(getConfig().getString("key"), "https://www.hievents.net/licenses/verify.php", this).register()){
            getServer().getPluginManager().disablePlugin(this);
            return;
        }*/
        plugin = this;
        saveDefaultConfig();

        classManager = new ClassManager(this);
        classManager.init();
        classManager.getCmdManager().initCMDs();
        utils = classManager.getUtils();
        itemsCache = new ItemsCache(this);

        if (!checkDependencies()){
            getServer().getPluginManager().disablePlugin(this);
        }

        if (getConfig().get("lobby.spawn")!=null){
            spawn = LocationUtil.getLocation(getConfig().getString("lobby.spawn"));
        }

        plugin.getClassManager().getUtils().startTab(false);
        pluginMessageManager = new PluginMessageManager(this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        boolean redisEnabled = getConfig().getBoolean("redis.enabled", false);

        if (redisEnabled) {
            redis = new Redis(this);
            redis.connect();
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (tries >= 3) {
                        cancel();
                        return;
                    }
                    if (redis.isActive()){
                        init();
                    } else {
                        getServer().getScheduler().runTaskLater(SpigotMain.this, this, 20);
                        tries++;
                    }
                }
            }.runTaskLater(this, 20L);

            new BukkitRunnable() {
                @Override
                public void run() {
                    loaded = true;
                }
            }.runTaskLater(this, 20L*5);
        } else {
            try {
                serverName = getServer().getServerName();
                if (serverManager != null) {
                    serverName = getServerManager().getActualServer().getName();
                }
                if (serverName.contains("-") || serverName.contains(" ")) {
                    serverName = serverName.replace("-", "_");
                    serverName = serverName.replace(" ", "_");

                    String[] split = serverName.split("_");
                    try {
                        Integer.parseInt(split[1]);
                        id = split[1];
                    } catch (Exception e) {
                        id = 1 + "";
                    }
                }
            } catch (NoSuchMethodError e) {
                serverName = "lobby";
                id = "1";
            }
            loaded = true;
        }
    }

    public void init(){
        File file = new File(getDataFolder(), "server.yml");
        if (!file.exists()){
            saveResource("server.yml", false);
        }
        FileUtils fileUtils = new FileUtils(file);
        String serverName = fileUtils.getString("server-name");
        String serverIp = fileUtils.getString("server-ip", getServer().getIp());
        serverIp = serverIp.replace("{server-ip}", getServer().getIp());
        String serverPort = fileUtils.getString("server-port", getServer().getPort()+"");
        serverPort = serverPort.replace("{server-port}", getServer().getPort()+"");
        String id = fileUtils.getString("id");
        if (serverName==null){
            console("{prefix}Server name not found in server.yml");
            return;
        }
        this.serverName = serverName;
        if (id==null){
            console("{prefix}Server id not found in server.yml");
            return;
        }
        this.id = id;
        redis.addServer(serverName, serverIp, serverPort);
        String info = "&fServer name: &b"+serverName+" &7| &fServer IP: &e"+serverIp+" &7| &fServer Port: &e"+serverPort;
        console("{prefix}&7Server connected to proxy and load server: "+info+"&7.");
    }

    private PAPI papi;

    public boolean checkDependencies(){
        PluginManager pm = getServer().getPluginManager();
        if (!pm.isPluginEnabled("PlaceholderAPI")){
            console("&4&lERROR: &cPlaceholderAPI doesn't found!");
            return false;
        } else {
            console("&aPlaceholderAPI found!");
            papi = new PAPI(this);
            papi.register();
            console("&fPlaceholderAPI hooked!");
        }

        if (pm.isPluginEnabled("LuckPerms")){
            console("&aLuckPerms found!");
            lp = new LP(this);
        }

        if (pm.isPluginEnabled("SkinsRestorer")){
            console("&aSkinsRestorer found!");
            sr = new SR(this);
        }

        if (pm.isPluginEnabled("TimoCloud")){
            console("&aTimoCloud found!");
            serverManager = new ServerManager(this);
        }

        return true;
    }

    public boolean haveLP(){
        return lp != null && getConfig().getBoolean("hooks.luckperms");
    }

    public boolean haveSR(){
        return sr != null && getConfig().getBoolean("hooks.skinsrestorer");
    }

    public static SpigotMain getPlugin() {
        return plugin;
    }

    public String getPrefix(){
        return Utils.ct(getConfig().getString("settings.prefix"));
    }

    public void console(String... in){
        getClassManager().getUtils().sendMessage(in);
    }

    public void debug(String in){
        if (!getConfig().getBoolean("settings.debug")){
            return;
        }
        if (classManager == null || classManager.getUtils() == null){
            Bukkit.getConsoleSender().sendMessage(Utils.ct(getPrefix() + "&e&lDEBUG: &7" + in));
            return;
        }
        getClassManager().getUtils().sendMessage("{prefix}&e&lDEBUG: &7"+in);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (loaded) {
            plugin.getClassManager().getUtils().startTab(true);
            if (plugin.getConfig().getBoolean("modules.nametags")){
                plugin.getClassManager().getNametagManager().init();
            }
            for (FUser user : plugin.getClassManager().getPlayerManager().getUsers().values()){
                user.getItemsManager().reloadItems();
                plugin.getClassManager().getMenusManager().loadMenus(user.getPlayer());
            }
        }
    }

    @Override
    public void onDisable() {
        if (redis == null){
            return;
        }
        if (redis.isActive() && serverName != null){
            redis.removeServer(serverName);
        }
        if (papi != null){
            papi.unregister();
        }
    }
}