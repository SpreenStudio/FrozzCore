package me.thejokerdev.frozzcore.cache;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cache {

    public long lastUsage;


    public Cache() {
        lastUsage = 0;
    }

}
