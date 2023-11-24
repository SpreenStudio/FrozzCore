package me.thejokerdev.frozzcore.cmds.internal.message;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.cmds.CMD;
import me.thejokerdev.frozzcore.managers.Managers;
import me.thejokerdev.frozzcore.managers.RedisCacheManager;
import me.thejokerdev.frozzcore.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Reply extends CMD {

    public Reply(BungeeMain plugin) {
        super(plugin, "reply", "responder", "r");
    }

    @Override
    public String name() {
        return "reply";
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList("responder", "r");
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
            if (args.length < 1) {
                plugin.getUtils().sendMessage(sender, "reply.usage");
            } else {
                UUID userLastPM = Managers.lastPrivateMessage.get(player.getUniqueId());
                if (userLastPM == null) {
                    plugin.getUtils().sendMessage(sender, "reply.noPlayer");
                } else {
                    String other = RedisCacheManager.get(plugin).getName(userLastPM);
                    if (other == null) {
                        plugin.getUtils().sendMessage(sender, "general.playerNotExists");
                        Managers.lastPrivateMessage.remove(player.getUniqueId());
                    } else if (other.equalsIgnoreCase(player.getName())) {
                        plugin.getUtils().sendMessage(sender, "general.notYou");
                    } else {
                        if (Managers.isMsgToggled(other)){
                            String str = plugin.getMessages().getString("message.toggled.cant-send");
                            str = str.replace("{player}", other);
                            plugin.getUtils().sendMessage(sender, str);
                            return;
                        }
                        String message = Utils.arrayJoin(args);
                        Managers.lastPrivateMessage.put(userLastPM, player.getUniqueId());

                        RedisCacheManager.get(plugin).sendMessage(userLastPM, plugin.getUtils().getMSG(null, plugin.getMessages().getString("reply.format.other").replace("{player}", player.getName()).replace("{msg}", message)));
                        plugin.getUtils().sendMessage(player, plugin.getMessages().getString("reply.format.player").replace("{player}", other).replace("{msg}", message));
                        plugin.getProxy().getPlayers().stream().filter((p) -> p.hasPermission("frozzcore.socialspy")).forEach((p) -> {
                            if (Managers.isSpy(p)) {
                                plugin.getUtils().sendMessage(p, plugin.getMessages().getString("socialspy.msg").replace("{player}", player.getName()).replace("{other}", other).replace("{msg}", message));
                            }
                        });
                    }
                }
            }
        }
    }
}
