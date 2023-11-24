package me.thejokerdev.frozzcore;

import litebans.api.Database;
import lombok.Getter;
import me.thejokerdev.frozzcore.cmds.CMDManager;
import me.thejokerdev.frozzcore.events.ABPSEvents;
import me.thejokerdev.frozzcore.events.ChatEvents;
import me.thejokerdev.frozzcore.events.LoginEvents;
import me.thejokerdev.frozzcore.events.PingEvents;
import me.thejokerdev.frozzcore.groups.GroupsManager;
import me.thejokerdev.frozzcore.managers.Placeholders;
import me.thejokerdev.frozzcore.managers.PlatformManager;
import me.thejokerdev.frozzcore.managers.RedisCacheManager;
import me.thejokerdev.frozzcore.redis.Redis;
import me.thejokerdev.frozzcore.utils.FileUtils;
import me.thejokerdev.frozzcore.utils.Utils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public final class BungeeMain extends Plugin {

    private LuckPerms api;
    private Database sanctions;
    private FileUtils fileUtils;
    private Utils utils;
    private Placeholders placeholders;
    private CMDManager cmdManager;
    private PlatformManager platformManager;
    private GroupsManager groupsManager;

    //Redis
    private Redis redis;

    //Plugin messages
    private final String pluginMessageChannel = "proxyutils:main";


    @Override
    public ProxyServer getProxy() {
        return super.getProxy();
    }

    @Getter
    private boolean enabledEvents = false;

    public void setEnabledEvents(boolean enabledEvents) {
        this.enabledEvents = enabledEvents;
    }

    @Override
    public void onEnable() {
        long ms = System.currentTimeMillis();

        api = LuckPermsProvider.get();
        sanctions = Database.get();

        loadClasses();
        loadEvents();

        redis = new Redis(this);
        redis.connect();

        getProxy().registerChannel(pluginMessageChannel);

        ms = System.currentTimeMillis()-ms;
        log("{prefix}&a"+getDescription().getName()+" enabled correctly in &e"+ms+"&a ms.");
    }

    public void log(String... msg){
        utils.sendMessage(getProxy().getConsole(), msg);
    }

    void loadEvents(){
        PluginManager pm = getProxy().getPluginManager();
        pm.registerListener(this, new LoginEvents(this));
        pm.registerListener(this, new ABPSEvents(this));
        pm.registerListener(this, new ChatEvents(this));
        pm.registerListener(this, new PingEvents(this));
    }

    void loadClasses(){
        fileUtils = new FileUtils(this);
        fileUtils.reloadConfig(this);
        utils = new Utils(this);
        utils.initTask();

        placeholders = new Placeholders(this);
        placeholders.init();
        log("{prefix}&eStarting "+getDescription().getName()+"...");

        cmdManager = new CMDManager(this);
        cmdManager.init();

        platformManager = new PlatformManager(this);
        platformManager.loadPlataforms();

        groupsManager = new GroupsManager(this);
        groupsManager.reload();

        checkServers();

        int player_limit = getConfig().getInt("settings.player-limit");
        if (player_limit > 0){
            try {
                changeSlots(player_limit);
            } catch (ReflectiveOperationException ignored) {
            }
        }
    }

    public void changeSlots(int slots) throws ReflectiveOperationException {
        Class<?> configClass = getProxy().getConfig().getClass();

        if (!configClass.getSuperclass().equals(Object.class)) {
            configClass = configClass.getSuperclass();
        }

        Field playerLimitField = configClass.getDeclaredField("playerLimit");
        playerLimitField.setAccessible(true);
        playerLimitField.setInt(getProxy().getConfig(), slots);
    }

    public boolean isOnlineMode(){
        return getConfig().getBoolean("settings.online-mode", true);
    }

    public Configuration getConfig(){
        return fileUtils.getConfig();
    }

    public void saveConfig() {
        fileUtils.saveConfig();
    }
    public Configuration getMessages(){
        return fileUtils.getMessages();
    }

    public User getUser(String name){
        return api.getUserManager().getUser(name);
    }

    public void reloadConfig(){
        fileUtils.reloadConfig(this);
        cmdManager.init();
        platformManager.loadPlataforms();
        utils.initTask();
        checkServers();
    }

    public void checkServers(){
        List<String> list = new ArrayList<>(getFileUtils().getServersCache().getStringList("servers"));
        for (String s : list) {
            String[] split = s.split(",");
            String name = split[0];
            String ip = split[1];
            int port = Integer.parseInt(split[2]);
            if (getProxy().getServers().containsKey(name)) {
                continue;
            }
            getProxy().getServers().put(name, getProxy().constructServerInfo(name, new InetSocketAddress(ip, port), "", false));
            String info = "&fServer name: &b" + name + " &7| &fServer IP: &e" + ip + " &7| &fServer Port: &e" + port;
            log("{prefix}&aAdded cached server to proxy -> " + info);
        }
    }


    public User getUser(ProxiedPlayer p){
        return api.getUserManager().getUser(p.getUniqueId());
    }


    public String getGroup(ProxiedPlayer p){
        User user = getUser(p);
        return user.getPrimaryGroup();
    }

    public String getPrefix(ProxiedPlayer player){
        String prefix = getConfig().getString("settings.name-mode").equalsIgnoreCase("prefix") ? getUser(player.getName()).getCachedData().getMetaData().getPrefix() : getUser(player.getName()).getFriendlyName();
        return prefix == null ? "" : prefix;
    }

    @Override
    public void onDisable() {
        long ms = System.currentTimeMillis();

        if (this.redis != null && this.redis.isActive()) {
            RedisCacheManager.get(this).clearCacheFromRedis();
            this.redis.disconnect();
        }

        ms = System.currentTimeMillis()-ms;
        log("{prefix}&c"+getDescription().getName()+" disabled correctly in &e"+ms+"&c ms.");
    }

    public String getProxyName() {
        return getConfig().getString("settings.proxyName", "Proxy-1");
    }

    public void debug(String... msg) {
        if (getConfig().getBoolean("settings.debug"))
            Arrays.stream(msg).forEach(s -> log("{prefix}&e[DEBUG] &f" + s));
    }
}
