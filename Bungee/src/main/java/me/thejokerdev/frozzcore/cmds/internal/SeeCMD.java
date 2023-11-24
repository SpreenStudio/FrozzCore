package me.thejokerdev.frozzcore.cmds.internal;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.cmds.CMD;
import me.thejokerdev.frozzcore.utils.StringUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SeeCMD extends CMD {
    public SeeCMD(BungeeMain plugin) {
        super(plugin, "see", "ver");
    }

    public String name() {
        return "see";
    }

    public String permission() {
        return "proxyutils.see";
    }

    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            plugin.getUtils().sendMessage(sender, "general.onlyPlayers");
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) sender;
        if (!check(p, permission())) {
            return;
        }
        if (args.length == 0) {
            plugin.getUtils().sendMessage(sender, "&cDebes especificar un jugador.");
            return;
        }
        ProxiedPlayer target = plugin.getProxy().getPlayer(args[0]);
        if (target == null) {
            plugin.getUtils().sendMessage(sender, "&cEl jugador especificado no existe o no estconectado.");
            return;
        }
        ServerInfo server = target.getServer().getInfo();
        if (server == null) {
            plugin.getUtils().sendMessage(sender, "&cEl jugador especificado no estconectado.");
            return;
        }
        if (p == target) {
            plugin.getUtils().sendMessage(sender, "&cNo puedes ejecutar este comando sobre ti mismo.");
            return;
        }
        JSONObject json = new JSONObject();
        json.put("staff", p.getName());
        json.put("target", target.getName());
        plugin.getRedis().getRedisManager().publish("staffmode", json.toString());
        if (p.getServer().getInfo() != server) {
            p.connect(server);
            plugin.getUtils().sendMessage(sender, "&aConectando a &e" + server.getName() + "&a...");
        }
    }

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (!justCheck(sender, permission()) || args.length != 1) {
            return new ArrayList<>();
        }
        return StringUtil.copyPartialMatches(args[0], plugin.getProxy().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList()), new ArrayList<>());
    }
}
