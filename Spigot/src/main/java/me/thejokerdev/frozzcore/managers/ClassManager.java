package me.thejokerdev.frozzcore.managers;

import lombok.Getter;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.api.LangDownloader;
import me.thejokerdev.frozzcore.api.board.ScoreBoard;
import me.thejokerdev.frozzcore.api.nametag.NametagHandler;
import me.thejokerdev.frozzcore.api.nametag.NametagManager;
import me.thejokerdev.frozzcore.api.utils.Utils;
import me.thejokerdev.frozzcore.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.Arrays;

@Getter
public class ClassManager {
    private final SpigotMain plugin;

    /* Managers */
    private CMDManager cmdManager;
    private MenusManager menusManager;
    private LangManager langManager;
    private LangDownloader langDownloader;
    private PlayerManager playerManager;
    private DataManager dataManager;

    private NametagManager nametagManager;
    private NametagHandler nametagHandler;
    private LinkedChatManager linkedChatManager;
    private EasterEggManager easterEggManager;

    private ConnectionListener connectionListener;
    private ScoreBoard scoreBoard;

    /* Utils */
    private Utils utils;

    public ClassManager(SpigotMain plugin) {
        this.plugin = plugin;

        initManagers();
    }

    public void initManagers() {
        cmdManager = new CMDManager(plugin);
        playerManager = new PlayerManager(plugin);
        dataManager = new DataManager(plugin);
        menusManager = new MenusManager(plugin);

        /* Languages */
        if (isModuleEnabled("languages")) {
            langDownloader = new LangDownloader(plugin);
            langManager = new LangManager(plugin);
        }
        /* Lobby */
        if (isModuleEnabled("lobby")) {
            regListener(new LobbyListener(plugin));
        }
        if (isModuleEnabled("chat")) {
            regListener(new ChatListener(plugin));
        }

        if (isModuleEnabled("nametags")) {
            nametagManager = new NametagManager(plugin);
            nametagHandler = new NametagHandler(plugin);
        }

        utils = new Utils(plugin);

        scoreBoard = new ScoreBoard(plugin);
        connectionListener = new ConnectionListener(plugin);

        regListener(connectionListener, new WorldListeners(plugin), new ItemEvents(plugin), new DoubleJump(plugin), new JumpPadsListener(plugin), new PluginListener(plugin));
    }

    public void init() {
        if (langManager != null){
            langManager.init();
        }
        if (nametagManager != null){
            nametagManager.init();
        }
        if (isModuleEnabled("scoreboard")){
            scoreBoard.loadAll();
        }
        if(plugin.getConfig().getBoolean("modules.eastereggs")){
            easterEggManager = new EasterEggManager(plugin);
            regListener(new EasterEggListener(plugin));
        }
    }

    public void initAfterStart(){
        if (plugin.getRedis() != null) {
            if (isModuleEnabled("linked-chat")) {
                linkedChatManager = new LinkedChatManager(plugin);
            } else {
                plugin.getLogger().severe("disabled linked-chat");
            }
            if (linkedChatManager != null) {
                linkedChatManager.init();
            }
        }
    }

    private boolean isModuleEnabled(String name) {
        return plugin.getConfig().getBoolean("modules." + name);
    }

    public void regListener(Listener... listener) {
        Arrays.stream(listener).forEach(listener1 -> Bukkit.getServer().getPluginManager().registerEvents(listener1, plugin));
    }
}
