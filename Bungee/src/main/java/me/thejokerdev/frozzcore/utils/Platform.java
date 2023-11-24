package me.thejokerdev.frozzcore.utils;

import lombok.Getter;
import net.md_5.bungee.config.Configuration;

import java.util.List;

@Getter
public class Platform {
    String id;
    String displayName;
    List<String> links;

    public Platform(String name, Configuration section){
        id = name;
        displayName = section.getString("name");
        links = section.getStringList("links");
    }
}
