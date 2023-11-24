package me.thejokerdev.frozzcore.cmds.internal.message;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.cmds.CMD;
import me.thejokerdev.frozzcore.managers.Managers;
import me.thejokerdev.frozzcore.managers.RedisCacheManager;
import me.thejokerdev.frozzcore.utils.StringUtil;
import me.thejokerdev.frozzcore.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

public class MSG extends CMD {

    public MSG(BungeeMain plugin) {
        super(plugin, "message", "msg", "tell", "mensaje", "m");
    }

    @Override
    public String name() {
        return "message";
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList("msg", "tell", "mensaje", "m", "minecraft:tell", "minecraft:msg", "minecraft:message", "minecraft:m", "minecraft:whisper", "minecraft:w", "whisper", "w", "emsg", "essentialsmsg", "essentials:emsg", "essentials:msg");
    }

    @Override
    public String permission() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            plugin.getUtils().sendMessage(sender, "general.onlyPlayers");
        } else {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            String ip = ((InetSocketAddress) player.getSocketAddress()).getAddress().getHostAddress();
            if (plugin.getSanctions().isPlayerMuted(player.getUniqueId(), ip)) {
                plugin.getUtils().sendMessage(sender, "general.muted");
                return;
            }
            if (args.length < 2) {
                if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
                    if (Managers.isMsgToggled(player)){
                        Managers.removeMsgToggled(player);
                    } else {
                        Managers.addMsgToggled(player);
                    }
                    plugin.getUtils().sendMessage(player, "message.toggled." + (Managers.isMsgToggled(player) ? "off" : "on"));
                    return;
                }
                plugin.getUtils().sendMessage(sender, "message.usage");
            } else {
                String playerName = args[0];
                UUID otherUUID = RedisCacheManager.get(plugin).getUUIDFromName(playerName);
                if (otherUUID == null) {
                    plugin.getUtils().sendMessage(sender, "general.playerNotExists");
                } else {
                    String name = RedisCacheManager.get(plugin).getName(otherUUID);
                    if (playerName.equalsIgnoreCase(player.getName())) {
                        plugin.getUtils().sendMessage(sender, "general.notYou");
                    } else {
                        if (Managers.isMsgToggled(name)){
                            String str = plugin.getMessages().getString("message.toggled.cant-send");
                            str = str.replace("{player}", name);
                            plugin.getUtils().sendMessage(sender, str);
                            return;
                        }
                        Vector<String> vector = Arrays.stream(args).collect(Collectors.toCollection(Vector::new));
                        vector.remove(0);
                        args = vector.toArray(new String[0]);
                        String message = Utils.arrayJoin(args);
                        Managers.lastPrivateMessage.put(player.getUniqueId(), otherUUID);
                        Managers.lastPrivateMessage.put(otherUUID, player.getUniqueId());
                        RedisCacheManager.get(plugin).sendMessage(otherUUID, plugin.getUtils().getMSG(null, plugin.getMessages().getString("message.format.other").replace("{player}", player.getName()).replace("{msg}", message)));
                        plugin.getUtils().sendMessage(sender, plugin.getMessages().getString("message.format.player").replace("{player}", name).replace("{msg}", message));
                        plugin.getProxy().getPlayers().stream().filter((p) -> p.hasPermission("proxyutils.socialspy")).forEach((p) -> {
                            if (Managers.isSpy(p)) {
                                plugin.getUtils().sendMessage(p, plugin.getMessages().getString("socialspy.msg").replace("{player}", player.getName()).replace("{other}", name).replace("{msg}", message));
                            }
                        });
                    }
                }
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("toggle");
            list.addAll(RedisCacheManager.get(plugin).getPlayers(args[0]));
            return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>());
        }
        return list;
    }
}
