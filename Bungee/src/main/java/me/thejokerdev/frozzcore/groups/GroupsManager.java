package me.thejokerdev.frozzcore.groups;

import me.thejokerdev.frozzcore.BungeeMain;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GroupsManager {
  private final BungeeMain plugin;
  
  private final HashMap<String, ServersGroup> serversGroups = new HashMap<>();
  
  public GroupsManager(BungeeMain plugin) {
    this.plugin = plugin;
  }
  
  public void reload() {
    this.plugin.debug("Reloading groups...");
    this.serversGroups.clear();
    if (this.plugin.getConfig().get("server-groups") == null)
      return; 
    List<String> keys = new ArrayList<>(this.plugin.getConfig().getSection("server-groups").getKeys());
    for (String group : keys) {
      this.plugin.debug("Loading group &e" + group + "&f...");
      this.serversGroups.put(group, new ServersGroup(this.plugin, group));
    } 
  }
  
  public ServersGroup getGroup(String name) {
    return this.serversGroups.get(name);
  }
  
  public ServersGroup getGroup(ServerInfo server) {
    for (ServersGroup group : this.serversGroups.values()) {
      if (group.isServerInGroup(server))
        return group; 
    } 
    this.plugin.debug("Couldn't find group for server &e" + server.getName() + "&f.");
    return null;
  }
  
  public List<ServersGroup> getGroups() {
    return new ArrayList<>(this.serversGroups.values());
  }
  
  public boolean isServerGroup(String name) {
    return this.serversGroups.containsKey(name);
  }
}
