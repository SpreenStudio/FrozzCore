package me.thejokerdev.frozzcore.cmds.internal;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.cmds.CMD;
import me.thejokerdev.frozzcore.utils.StringUtil;
import net.md_5.bungee.api.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BWhitelist extends CMD {
    public BWhitelist(BungeeMain plugin) {
        super(plugin, "bwhitelist", "bwl");
    }

    @Override
    public String name() {
        return "bwhitelist";
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList("bwl");
    }

    @Override
    public String permission() {
        return "proxyutils.bwhitelist";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!check(sender, permission())){
            return;
        }
        if (args.length == 0){
            plugin.getUtils().sendMessage(sender, "{prefix}&cUso: /bwhitelist <add/remove/setlimit> <player/limit>");
        } else {
            String var1 = args[0].toLowerCase();
            List<String> list = new ArrayList<>(plugin.getFileUtils().getWhitelist().getStringList("list"));
            list = list.stream().map(String::toLowerCase).collect(Collectors.toList());
            if (var1.equals("remove")){
                if (args.length == 1){
                    plugin.getUtils().sendMessage(sender, "{prefix}&cUso: /bwhitelist remove <player>");
                    return;
                }
                String var2 = args[1].toLowerCase();
                if (list.contains(var2)){
                    list.remove(var2);
                    plugin.getFileUtils().getWhitelist().set("list", list);
                    plugin.getFileUtils().saveWhitelist();
                    plugin.getUtils().sendMessage(sender, "{prefix}&aJugador &e" + var2 + " &aquitado de la lista blanca.");
                } else {
                    plugin.getUtils().sendMessage(sender, "{prefix}&cEl jugador &e" + var2 + " &cno está en la lista blanca.");
                }
            }
            if (var1.equals("add")){
                if (args.length == 1){
                    plugin.getUtils().sendMessage(sender, "{prefix}&cUso: /bwhitelist add <player>");
                    return;
                }
                String var2 = args[1].toLowerCase();
                if (!list.contains(var2)){
                    list.add(var2);
                    plugin.getFileUtils().getWhitelist().set("list", list);
                    plugin.getFileUtils().saveWhitelist();
                    plugin.getUtils().sendMessage(sender, "{prefix}&aJugador &e" + var2 + " &aagregado a la lista blanca.");
                } else {
                    plugin.getUtils().sendMessage(sender, "{prefix}&cEl jugador &e" + var2 + " &cya está en la lista blanca.");
                }
            }
            if (var1.equals("setlimit")){
                if (args.length == 1){
                    plugin.getUtils().sendMessage(sender, "{prefix}&cUso: /bwhitelist setlimit <limit>");
                    return;
                }
                String var2 = args[1];
                try {
                    int limit = Integer.parseInt(var2);
                    if (limit < 0){
                        plugin.getUtils().sendMessage(sender, "{prefix}&cEl límite debe ser mayor o igual a 0.");
                        return;
                    }
                    plugin.getFileUtils().getWhitelist().set("settings.limit", limit);
                    plugin.getFileUtils().saveWhitelist();
                    plugin.getUtils().sendMessage(sender, "{prefix}&aLímite de jugadores en la lista blanca establecido a &e" + limit);
                } catch (NumberFormatException e){
                    plugin.getUtils().sendMessage(sender, "{prefix}&cEl límite debe ser un número.");
                    return;
                }
            }
            if (var1.equals("togglefill")){
                boolean fill = plugin.getFileUtils().getWhitelist().getBoolean("settings.allow-fill");
                plugin.getFileUtils().getWhitelist().set("settings.allow-fill", !fill);
                plugin.getFileUtils().saveWhitelist();
                plugin.getUtils().sendMessage(sender, "{prefix}&aEl permitir entrar a personas que no estén en la lista ha sido &e" + (!fill ? "activado" : "desactivado"));
            }
            if (var1.equals("toggleevents")){
                boolean toggle = !plugin.isEnabledEvents();
                plugin.setEnabledEvents(toggle);

                plugin.getUtils().sendMessage(sender, "{prefix}&aLos eventos de la lista blanca han sido &e" + ( toggle ? "activados" : "desactivados"));
                return;
            }
            if (var1.equals("toggle")){
                boolean enabled = plugin.getFileUtils().getWhitelist().getBoolean("settings.enabled");
                plugin.getFileUtils().getWhitelist().set("settings.enabled", !enabled);
                plugin.getFileUtils().saveWhitelist();
                plugin.getUtils().sendMessage(sender, "{prefix}&aLa lista blanca ha sido &e" + (!enabled ? "activada" : "desactivada"));
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!justCheck(sender, permission())){
            return new ArrayList<>();
        }
        if (args.length == 1){
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("add", "remove", "setlimit", "togglefill", "toggle", "toggleevents"), new ArrayList<>());
        }
        if (args.length == 2){
            String var1 = args[0].toLowerCase();
            List<String> list = new ArrayList<>(plugin.getFileUtils().getWhitelist().getStringList("list"));
            if (var1.equalsIgnoreCase("remove")){
                return StringUtil.copyPartialMatches(args[1], list, new ArrayList<>());
            }
            if (var1.equals("setlimit")){
                list.add(String.valueOf(plugin.getFileUtils().getWhitelist().getInt("settings.limit")));
                return list;
            }
        }
        return super.onTabComplete(sender, args);
    }
}
