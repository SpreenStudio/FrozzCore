package me.thejokerdev.frozzcore.managers;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Managers {

    private static final List<String> toggled = new ArrayList<>();
    private static final List<String> hided = new ArrayList<>();
    private static final List<String> socialspy = new ArrayList<>();

    private static final List<String> msgToggle = new ArrayList<>();
    public static HashMap<UUID, UUID> lastPrivateMessage = new HashMap<>();

    public static boolean isToggled(ProxiedPlayer p){
        return toggled.contains(p.getName());
    }

    public static boolean isHided(ProxiedPlayer p){
        return hided.contains(p.getName());
    }
    public static boolean isSpy(ProxiedPlayer p){
        return socialspy.contains(p.getName());
    }

    public static boolean isMsgToggled(ProxiedPlayer p){
        return msgToggle.contains(p.getName());
    }
    public static boolean isMsgToggled(String p){
        return msgToggle.contains(p);
    }

    public static void addToggled(ProxiedPlayer p){
        if (!toggled.contains(p.getName())){
            toggled.add(p.getName());
        }
    }

    public static void removeToggled(ProxiedPlayer p){
        toggled.remove(p.getName());
    }

    public static void addMsgToggled(ProxiedPlayer p){
        if (!msgToggle.contains(p.getName())){
            msgToggle.add(p.getName());
        }
    }

    public static void removeMsgToggled(ProxiedPlayer p){
        msgToggle.remove(p.getName());
    }


    public static void addHided(ProxiedPlayer p){
        if (!hided.contains(p.getName())){
            hided.add(p.getName());
        }
    }

    public static void removeHided(ProxiedPlayer p){
        hided.remove(p.getName());
    }

    public static void addSocialSpy(ProxiedPlayer p){
        if (!socialspy.contains(p.getName())){
            socialspy.add(p.getName());
        }
    }

    public static void removeSocialSpy(ProxiedPlayer p){
        socialspy.remove(p.getName());
    }
}