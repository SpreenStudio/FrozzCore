package me.thejokerdev.frozzcore.cmds.internal;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.cmds.CMD;
import me.thejokerdev.frozzcore.managers.Managers;
import me.thejokerdev.frozzcore.managers.Permissions;
import me.thejokerdev.frozzcore.utils.StringUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StaffChat extends CMD {
    public StaffChat(BungeeMain plugin) {
        super(plugin, "staffchat", "sc");
    }

    @Override
    public String name() {
        return getName();
    }

    @Override
    public List<String> aliases() {
        return Arrays.stream(getAliases()).collect(Collectors.toList());
    }

    @Override
    public String getPermission() {
        return Permissions.STAFFCHAT_STAFF.get();
    }

    @Override
    public String permission() {
        return getPermission();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!check(sender, permission())) {
            return;
        }
        if (!(sender instanceof ProxiedPlayer)) {
            if (args.length == 1){
                String arg = args[0];
                ProxiedPlayer t = plugin.getProxy().getPlayer(arg);
                if (t == null){
                    plugin.getUtils().sendMessage(sender, "{prefix}&cEse jugador no está conectado.");
                    return;
                }
                if (!t.hasPermission(Permissions.STAFFCHAT_STAFF.get())) {
                    plugin.getUtils().sendMessage(sender, "general.noPermissions");
                    return;
                }
                toggle(t);
                return;
            }
            if (args.length > 0) {
                String format = plugin.getConfig().getString("staffchat.format-console");
                format = plugin.getUtils().getMSG(null, format).getText();

                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < args.length; i++) {
                    sb.append(args[i]);
                    if (i != args.length - 1) {
                        sb.append(" ");
                    }
                }
                plugin.getUtils().sendMSGtoStaff(null, format + sb);
            }
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) sender;

        if (!p.hasPermission(Permissions.STAFFCHAT_STAFF.get())) {
            plugin.getUtils().sendMessage(sender, "general.noPermissions");
            return;
        }

        if (args.length == 0){
            toggle(p);
            return;
        }

        String arg1 = args[0].toLowerCase();
        switch (arg1) {
            case "hide": {
                if (Managers.isHided(p)) {
                    Managers.removeHided(p);
                    plugin.getUtils().sendMessage(p, "staffchat.cmd.hide.disabled");
                } else {
                    Managers.addHided(p);
                    plugin.getUtils().sendMessage(p, "staffchat.cmd.hide.enabled");
                }
                break;
            }
            case "toggle": toggle(p); break;
            default :{
                String format = plugin.getConfig().getString("staffchat.format");
                format = plugin.getUtils().getMSG(p, format).getText();

                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < args.length; i++) {
                    sb.append(args[i]);
                    if (i != args.length - 1) {
                        sb.append(" ");
                    }
                }
                plugin.getUtils().sendMSGtoStaff(p, format + sb);
                break;
            }
        }
    }

    public void toggle(ProxiedPlayer p) {
        String msgT = plugin.getMessages().getString("staffchat.cmd.toggled");


        if (Managers.isToggled(p)) {
            msgT = msgT.replace("%status%", plugin.getMessages().getString("status-off"));
            Managers.removeToggled(p);
        } else {
            msgT = msgT.replace("%status%", plugin.getMessages().getString("status-on"));
            Managers.addToggled(p);
        }

        plugin.getUtils().sendMessage(p, msgT);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> back = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission(Permissions.STAFFCHAT_STAFF.get())){
            List<String> list = new ArrayList<>(Arrays.asList(
                    "hide",
                    "toggle"
            ));
            StringUtil.copyPartialMatches(args[0], list, back);
        }
        return back;
    }
}
