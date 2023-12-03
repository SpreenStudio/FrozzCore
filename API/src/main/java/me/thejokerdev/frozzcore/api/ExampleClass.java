package me.thejokerdev.frozzcore.api;

import org.bukkit.entity.Player;

public class ExampleClass {
    private final FrozzCoreAPI api;

    public ExampleClass(){
        api = new FrozzCoreAPI();

        /* ==| Send message to J0keer using translation |== */
        Player p = api.getPlugin().getServer().getPlayer("J0keer");
        api.sendMSG(p, "example", "general.testMessage", p.getName());
        //Message sended!
    }
}