package me.thejokerdev.frozzcore;

import lombok.Getter;
import lombok.Setter;
import me.thejokerdev.frozzcore.api.cache.ItemsCache;
import me.thejokerdev.frozzcore.api.events.RedisInitEvent;
import me.thejokerdev.frozzcore.api.hooks.LuckPermsHook;
import me.thejokerdev.frozzcore.api.hooks.PapiExpansion;
import me.thejokerdev.frozzcore.api.hooks.SkinsRestorerHook;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

@Getter
@Setter
public final class SpigotMain extends JavaPlugin {

    @Getter
    private static SpigotMain plugin;
    private ClassManager classManager;
    private ItemsCache itemsCache;
    private PluginMessageManager pluginMessageManager;

    private LuckPermsHook luckPerms = null;
    private SkinsRestorerHook skinsRestorer = null;
    private Location spawn = null;
    public Utils utils;

    private boolean loaded = false;

    // Messaging
    private Redis redis;

    private String serverId;
    private String serverName;
    int tries = 0;

    private ServerManager serverManager;

    private boolean nickAPIEnabled = false;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();

        classManager = new ClassManager(this);
        classManager.init();
        utils = classManager.getUtils();
        itemsCache = new ItemsCache(this);

        // If PlaceholderAPI not exists then: log &  disable this plugin
        if (!hasPlaceholderAPI()){
            console("&4&lERROR: &cPlaceholderAPI doesn't found!");
            getServer().getPluginManager().disablePlugin(this);
        }

        registerDependencies();

        // Register all online players
        getServer().getOnlinePlayers().forEach(p-> getClassManager().getPlayerManager().registerUser(p.getName(), p.getUniqueId()));

        classManager.getCmdManager().initCMDs();

        loadSpawnIfSet();

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
                        classManager.initAfterStart();
                        Bukkit.getPluginManager().callEvent(new RedisInitEvent(plugin, redis));
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
            // REDIS: DISABLED >>
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
                        serverId = split[1];
                    } catch (Exception e) {
                        serverId = 1 + "";
                    }
                }
            } catch (NoSuchMethodError e) {
                serverName = "lobby";
                serverId = "1";
            }
            loaded = true;
        }
    }

    public void init() {
        File file = new File(getDataFolder(), "server.yml");
        if (!file.exists()){
            saveResource("server.yml", false);
        }
        FileUtils fileUtils = new FileUtils(file);
        boolean enabled = fileUtils.getBoolean("enabled", false);
        if (!enabled){
            console("{prefix}&cServer not enabled in server.yml to add with Redis.");
            return;
        }
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
        this.serverId = id;
        redis.addServer(serverName, serverIp, serverPort);
        String info = "&fServer name: &b"+serverName+" &7| &fServer IP: &e"+serverIp+" &7| &fServer Port: &e"+serverPort;
        console("{prefix}&7Server connected to proxy and load server: "+info+"&7.");
    }

    private void loadSpawnIfSet() {
        if (getConfig().get("lobby.spawn") != null){
            spawn = LocationUtil.getLocation(getConfig().getString("lobby.spawn"));
        }
    }

    private PapiExpansion papiExpansion;

    public void registerDependencies(){
        checkDependencyPlugin("PlaceholderAPI", () -> {
            console("&aPlaceholderAPI found!");
            papiExpansion = new PapiExpansion(this);
            papiExpansion.register();
            console("&fPlaceholderAPI hooked!");
        });

        checkDependencyPlugin("NickAPI", () -> {
            console("&aNickAPI found!");
            setNickAPIEnabled(true);
        });

        checkDependencyPlugin("LuckPerms", () -> {
            console("&aLuckPerms found!");
            luckPerms = new LuckPermsHook(this);
        });

        checkDependencyPlugin("SkinsRestorer", () -> {
            console("&aSkinsRestorer found!");
            skinsRestorer = new SkinsRestorerHook(this);
        });


        checkDependencyPlugin("Cloud", () -> {
            console("&aCloud found!");
            serverManager = new ServerManager(this);
        });
    }

    private boolean hasPlaceholderAPI() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    private void checkDependencyPlugin(String name, Runnable function) {
        if (Bukkit.getPluginManager().isPluginEnabled(name)) {
            function.run();
        }
    }

    public boolean isLuckPermsEnabled() {
        return luckPerms != null && getConfig().getBoolean("hooks.luckperms");
    }

    public boolean isSkinsRestorerEnabled() {
        return skinsRestorer != null && getConfig().getBoolean("hooks.skinsrestorer");
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
<<<<<<< HEAD
        if (redis != null && redis.isActive()){
            if (serverName != null) redis.removeServer(serverName);
            redis.disconnect();
        }
        if (papi != null){
            papi.unregister();
        }
        if (getClassManager().getDataManager() != null && getClassManager().getDataManager().getData()!=null){
=======
        // Redis connection + server registration
        if (redis != null && redis.isActive()) {
            if (serverName != null) redis.removeServer(serverName);
            redis.disconnect();
        }
        // PlaceholderAPI
        if (papiExpansion != null) {
            papiExpansion.unregister();
        }
        // DataManager
        if (getClassManager().getDataManager() != null && getClassManager().getDataManager().getData() != null){
>>>>>>> 5ee417ba3f4dd8bddcda9ad6f51d2ce2c5187ba0
            getClassManager().getDataManager().getData().close();
        }
    }
}
