package me.thejokerdev.frozzcore.managers;

import me.thejokerdev.frozzcore.api.itemaction.*;
import me.thejokerdev.frozzcore.type.ItemActionExecutor;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class ItemActionManager {

    private static final HashMap<String, ItemActionExecutor> commands = new HashMap<>();

    public static void registerDefaults() {
        register("sound", new Sound());
        register("close", new Close());
        register("server", new Server());
        register("balancer", new Balancer());
        register("cmd", new Command());
        register("cmd=OP", new CommandOp());
        register("cmd=Console", new CommandConsole());
        register("msg", new Message());
        register("open", new Open());
        register("title", new Title());
        register("action", new Action());
        register("speed", new Speed());
    }

    public static void register(String name, ItemActionExecutor executor) {
        commands.put("[" + name + "]", executor);
    }

    public static void execute(Player player, String label) {
        String name = label.split("]")[0] + "]";
        ItemActionExecutor executor = commands.get(name);
        if (executor == null) return;
        executor.onCommand(player, label);
    }

}
