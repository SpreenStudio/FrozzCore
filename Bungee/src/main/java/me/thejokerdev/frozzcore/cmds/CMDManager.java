package me.thejokerdev.frozzcore.cmds;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.cmds.custom.CustomCMD;
import me.thejokerdev.frozzcore.cmds.internal.*;
import me.thejokerdev.frozzcore.cmds.internal.message.MSG;
import me.thejokerdev.frozzcore.cmds.internal.message.MSGToggle;
import me.thejokerdev.frozzcore.cmds.internal.message.Reply;
import me.thejokerdev.frozzcore.cmds.internal.message.SocialSpy;
import net.md_5.bungee.api.plugin.PluginManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CMDManager {
    private final BungeeMain plugin;
    private List<CMD> commands;
    private List<CMD> customCommands;
    private final File folder;

    public CMDManager(BungeeMain plugin){
        this.plugin = plugin;

        folder = new File(plugin.getDataFolder(), "commands/");
        if (!folder.exists()){
            folder.mkdir();
        }
    }

    public void  init(){
        commands = new ArrayList<>();

        commands.add(new StaffChat(plugin));
        commands.add(new ProxyUtils(plugin));
        commands.add(new Stream(plugin));
        commands.add(new PingCMD(plugin));
        commands.add(new MSG(plugin));
        commands.add(new Reply(plugin));
        commands.add(new SocialSpy(plugin));
        commands.add(new ProxyTP(plugin));
        commands.add(new MSGToggle(plugin));
        commands.add(new Maintenance(plugin));
        commands.add(new BWhitelist(plugin));
        commands.add(new SeeCMD(plugin));

        folder.listFiles();
        for (File f : folder.listFiles()) {
            if (f.getName().endsWith(".yml")) {
                commands.add(new CustomCMD(plugin, plugin.getFileUtils().load(f)));
            }
        }
        PluginManager manager = plugin.getProxy().getPluginManager();
        for (CMD cmd : commands){
            manager.registerCommand(plugin, cmd);
            plugin.log("{prefix}&aCommand loaded: "+cmd.getName());
        }
    }
}
