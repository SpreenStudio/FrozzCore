package me.thejokerdev.frozzcore.utils;

import com.google.common.io.ByteStreams;
import lombok.Getter;
import me.thejokerdev.frozzcore.BungeeMain;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;

@Getter
public class FileUtils {
    private final BungeeMain plugin;
    public Configuration config;
    public Configuration messages;
    public Configuration serversCache;
    public Configuration whitelist;
    public Configuration webhooks;
    public FileUtils(BungeeMain plugin) {
        this.plugin = plugin;
    }

    public void reloadConfig(BungeeMain plugin){
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(
                    loadResource(plugin, "config.yml"));
            messages = ConfigurationProvider.getProvider(YamlConfiguration.class).load(
                    loadResource(plugin, "messages.yml"));
            serversCache = ConfigurationProvider.getProvider(YamlConfiguration.class).load(
                    loadResource(plugin, "servers-cache.yml"));
            whitelist = ConfigurationProvider.getProvider(YamlConfiguration.class).load(
                    loadResource(plugin, "whitelist.yml"));
            webhooks = ConfigurationProvider.getProvider(YamlConfiguration.class).load(
                    loadResource(plugin, "webhooks.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Configuration load(File file) {
        if (file.exists()){
            try {
                return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return load(file);
        }
        return null;
    }

    public File loadResource(Plugin plugin, String resource) {
        File folder = plugin.getDataFolder();
        if (!folder.exists())
            folder.mkdir();
        File resourceFile = new File(folder, resource);
        try {
            if (!resourceFile.exists()) {
                resourceFile.createNewFile();
                try (InputStream in = plugin.getResourceAsStream(resource);
                     OutputStream out = new FileOutputStream(resourceFile)) {
                    ByteStreams.copy(in, out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resourceFile;
    }

    public void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveServersCache(){
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(serversCache, new File(plugin.getDataFolder(), "servers-cache.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveWhitelist(){
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(whitelist, new File(plugin.getDataFolder(), "whitelist.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveWebHooks(){
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(webhooks, new File(plugin.getDataFolder(), "webhooks.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
