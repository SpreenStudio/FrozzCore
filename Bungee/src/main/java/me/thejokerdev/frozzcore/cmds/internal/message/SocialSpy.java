package me.thejokerdev.frozzcore.cmds.internal.message;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.cmds.CMD;
import me.thejokerdev.frozzcore.managers.Managers;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SocialSpy extends CMD {
    public SocialSpy(BungeeMain plugin) {
        super(plugin, "socialspy");
    }

    @Override
    public String name() {
        return "socialspy";
    }

    @Override
    public String permission() {
        return "frozzcore.socialspy";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            plugin.getUtils().sendMessage(sender, "reply.usage");
        } else {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            if (!player.hasPermission(permission())) {
                plugin.getUtils().sendMessage(sender, "general.noPermissions");
            } else {
                if (Managers.isSpy(player)){
                    Managers.removeSocialSpy(player);
                } else {
                    Managers.addSocialSpy(player);
                }
                plugin.getUtils().sendMessage(player, "socialspy." + (Managers.isSpy(player) ? "enabled" : "disabled"));
            }
        }
    }
}
