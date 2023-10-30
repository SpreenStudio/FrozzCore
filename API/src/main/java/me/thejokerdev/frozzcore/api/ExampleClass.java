package me.thejokerdev.frozzcore.api;

import org.bukkit.entity.Player;

public class ExampleClass {
    private final FrozzCoreAPI api;

    public ExampleClass(){
        api = new FrozzCoreAPI();

        /* ==| Send message to J0keer using translation |== */
        Player p = FrozzCoreAPI.getPlugin().getServer().getPlayer("J0keer");
        api.sendMSG(p, "general", "general.testMessage", p.getName());
        //Message sended!
    }
}