package me.thejokerdev.frozzcore.hooks.webhooks;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.utils.FileUtils;

import java.util.HashMap;

public class WebhookManager {
    private final BungeeMain plugin;
    private final FileUtils fileUtils;

    private HashMap<String, WebHook> webhooks = new HashMap<>();

    public WebhookManager(BungeeMain plugin) {
        this.plugin = plugin;
        this.fileUtils = plugin.getFileUtils();
        load();
    }

    public void load(){
        FileUtils fileUtils = plugin.getFileUtils();
        for (String key : fileUtils.getWebhooks().getKeys()){
            WebHook webHook = new WebHook(plugin, fileUtils.getWebhooks().getSection(key));
            webhooks.put(key, webHook);
        }
    }

    public WebHook getWebHook(String name){
        return webhooks.get(name);
    }

    public WebHook getStaff(){
        return webhooks.get("staff").clone();
    }

    public WebHook getPermissions(){
        return webhooks.get("permissions").clone();
    }

    public WebHook getCloud(){
        return webhooks.get("cloud").clone();
    }
}
