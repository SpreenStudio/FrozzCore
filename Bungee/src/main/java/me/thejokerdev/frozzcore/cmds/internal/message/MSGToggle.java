package me.thejokerdev.frozzcore.cmds.internal.message;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.cmds.CMD;
import me.thejokerdev.frozzcore.managers.Managers;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class MSGToggle extends CMD {

    public MSGToggle(BungeeMain plugin) {
        super(plugin, "msgtoggle", "togglemsg", "togglemessage", "messagetoggle");
    }

    @Override
    public String name() {
        return "msgtoggle";
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
            if (Managers.isMsgToggled(player)){
                Managers.removeMsgToggled(player);
            } else {
                Managers.addMsgToggled(player);
            }
            plugin.getUtils().sendMessage(player, "message.toggled." + (Managers.isMsgToggled(player) ? "off" : "on"));
        }
    }
}
