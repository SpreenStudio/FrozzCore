package me.thejokerdev.frozzcore.managers;

import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.enums.BalanceType;
import me.thejokerdev.frozzcore.type.FUser;
import studio.spreen.cloud.api.CloudAPI;
import studio.spreen.cloud.api.objects.ProxyObject;
import studio.spreen.cloud.api.objects.ServerGroupObject;
import studio.spreen.cloud.api.objects.ServerObject;

import java.util.*;
import java.util.stream.Stream;

public class ServerManager {
    private final SpigotMain plugin;

    public ServerManager(SpigotMain plugin) {
        this.plugin = plugin;
    }

    public int getGlobalCount() {
        return CloudAPI.getUniversalAPI().getProxies().stream().mapToInt(ProxyObject::getOnlinePlayerCount).sum();
    }
    public ServerObject getServer(String name) {
        if (Objects.isNull(CloudAPI.getUniversalAPI().getServer(name))) {
            return null;
        }

        return getNullableServer(name);
    }

    public ServerObject getActualServer(){
        return CloudAPI.getBukkitAPI().getThisServer();
    }

    public ServerGroupObject getActualGroup(){
        return getActualServer().getGroup();
    }
    public ServerObject getNullableServer(String name) {
        return CloudAPI.getUniversalAPI().getServer(name);
    }
    public ServerGroupObject getNullableServerGroup(String name) {
        return CloudAPI.getUniversalAPI().getServerGroup(name);
    }
    public ServerGroupObject getServerGroup(String name) {
        ServerGroupObject serverGroupObject = CloudAPI.getUniversalAPI().getServerGroup(name);

        if (Objects.isNull(serverGroupObject)) {
            return null;
        }

        return serverGroupObject;
    }
    public boolean isMaintenance(String name) {
        return getNullableServer(name).getState().equals("MAINTENANCE");
    }
    public String getTranslatedStatus(FUser user, String name) {
        String status = "";
        String lang = user != null ? user.getLang() : plugin.getClassManager().getLangManager().getDefault();
        if (Objects.isNull(getNullableServerGroup(name))) {
            status = plugin.getClassManager().getLangManager().getLanguageOfSection("general", lang).getFile().getString("core.cloud.status.unavailable");
        /*} else if (isMaintenance(name)) {
            return new TranslatableComponent(user, "core.cloud.status.maintenance");*/
        } else {
            status = plugin.getClassManager().getLangManager().getLanguageOfSection("general", lang).getFile().getString("core.cloud.status.available");
        }
        status = plugin.getUtils().formatMSG(user != null ? user.getPlayer() : null, status);
        return status;
    }
    public int getServerOnlineAmount(String name) {
        if (Objects.isNull(getNullableServer(name))) {
            return 0;
        }

        return getNullableServer(name).getOnlinePlayerCount();
    }
    public int getGroupServerAmount(String name) {
        return getServerGroup(name).getOnlineAmount();
    }
    public int getServerMaxAmount(String name) {
        if (Objects.isNull(getNullableServer(name))) {
            return 0;
        }

        return getNullableServer(name).getMaxPlayerCount();
    }
    public int getGroupOnlineAmount(String name) {
        if (Objects.isNull(getServerGroup(name))) {
            return 0;
        }
        return CloudAPI.getUniversalAPI().getServerGroup(name).getServers().stream().filter(serverObject -> serverObject.getGroup().getName().equalsIgnoreCase(name)).mapToInt(ServerObject::getOnlinePlayerCount).sum();
    }
    public int getGroupMaxAmount(String name) {
        if (Objects.isNull(getServerGroup(name))) {
            return 0;
        }
        return CloudAPI.getUniversalAPI().getServerGroup(name).getMaxAmount();
    }

    public ServerGroupObject getGroup(String name) {
        return CloudAPI.getUniversalAPI().getServerGroup(name);
    }

    public int getGroupMaxPlayerAmount(String name) {
        if (Objects.isNull(getGroup(name))) {
            return 0;
        }

        return getGroup(name).getServers().stream().mapToInt(ServerObject::getMaxPlayerCount).sum();
    }
    public int getServerNumber(ServerObject serverObject) {
        List<String> list = Arrays.asList(serverObject.getName().split("-"));

        return Integer.parseInt(list.get(list.size() - 1));
    }
    public ServerObject getAvailableServer(BalanceType balanceType, String name) {
        return getAvailableServer(balanceType, getServerGroup(name).getServers());
    }
    public ServerObject getAvailableServer(BalanceType balanceType, Collection<ServerObject> serverObjects) {
        Stream<ServerObject> serverObjectList = serverObjects.stream();

        if (!serverObjectList.findAny().isPresent()) {
            return null;
        }

        switch (balanceType) {
            case LESS_PLAYERS:
                return serverObjects.stream().sorted(Comparator.comparing(ServerObject::getOnlinePlayerCount)).sorted().iterator().next();
            case NEXT_AVAILABLE:
                return serverObjects.stream().sorted(Comparator.comparing(ServerObject::getOnlinePlayerCount).reversed()).sorted().iterator().next();
        }
        return null;
    }

    public String getAvailableServerName(BalanceType balanceType, String name) {
        return getAvailableServer(balanceType, name).getName();
    }
}
