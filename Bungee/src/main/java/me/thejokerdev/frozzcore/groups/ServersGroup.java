package me.thejokerdev.frozzcore.groups;

import me.thejokerdev.frozzcore.BungeeMain;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

public class ServersGroup {
  private final BungeeMain plugin;
  
  private final String name;
  
  private String displayName;
  
  public BungeeMain getPlugin() {
    return this.plugin;
  }
  
  public String getName() {
    return this.name;
  }
  
  public String getDisplayName() {
    return this.displayName;
  }
  
  private List<String> servers = new ArrayList<>();
  
  private String regex;
  
  public List<String> getServers() {
    return this.servers;
  }
  
  public String getRegex() {
    return this.regex;
  }
  
  public ServersGroup(BungeeMain plugin, String name) {
    this.plugin = plugin;
    this.name = name;
    reload();
    plugin.debug("Loaded group &e" + name + "&f with servers: &a" + this.servers.toString() + "&f and regex: &a" + this.regex + "&f and display name: &b" + this.displayName + "&f.");
  }
  
  public void reload() {
    this.displayName = this.plugin.getConfig().getString("server-groups." + this.name + ".display-name", this.name);
    if (this.plugin.getConfig().get("server-groups." + this.name + ".servers") != null)
      this.servers = this.plugin.getConfig().getStringList("server-groups." + this.name + ".servers"); 
    this.regex = this.plugin.getConfig().getString("server-groups." + this.name + ".regex", null);
  }
  
  public boolean isServerInGroup(ProxiedPlayer player) {
    String server = player.getServer().getInfo().getName();
    if (this.regex != null)
      return server.matches(this.regex); 
    return this.servers.contains(server);
  }
  
  public boolean isServerInGroup(ServerInfo serverInfo) {
    String server = serverInfo.getName();
    if (this.regex != null)
      return server.matches(this.regex); 
    return this.servers.contains(server);
  }
}
