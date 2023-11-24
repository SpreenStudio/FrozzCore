package me.thejokerdev.frozzcore.utils;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.enums.MOTDMode;
import me.thejokerdev.frozzcore.managers.Managers;
import me.thejokerdev.frozzcore.managers.Permissions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private final BungeeMain plugin;
    public Utils (BungeeMain plugin){
        this.plugin = plugin;
    }

    //String utils
    public String ct(String msg){
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
    public String getCenteredMessage(String message){
        assert message != null;
        message = ct(message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : message.toCharArray()){
            if(c == '§'){
                previousCode = true;
            }else if(previousCode){
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            }else{
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = 154 - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
            sb.append("&r ");
            compensated += spaceLength;
        }
        return sb + message;
    }

    //integer utils
    public boolean isNumeric(String var0) {
        try {
            Integer.parseInt(var0);
            return true;
        } catch (NumberFormatException var2) {
            return false;
        }
    }
    public TextComponent getComponent(String in){
        TextComponent component = new TextComponent();

        String[] args = in.split(",");

        StringBuilder text = new StringBuilder();
        ChatColor color;
        boolean hasColor = in.contains("color:");
        boolean hasHover = in.contains("hover:");
        boolean hasClick = in.contains("click:");

        for (String s : args){
            if (s.startsWith("text:")){
                text.append(ct(s.replace("text:", "")));
                if (text.toString().contains(".") && !text.toString().contains(" ")){
                    text = new StringBuilder(plugin.getFileUtils().getMessages().getString(in));
                }
                component = new TextComponent(plugin.getPlaceholders().apply(ct(text.toString())));
                continue;
            }
            if (hasColor && s.startsWith("color:")){
                s = s.replace("color:", "");
                color = ChatColor.valueOf(s.toUpperCase());
                component.setColor(color);
                continue;
            }
            if (hasHover && s.startsWith("hover:")){
                s = s.replace("hover:", "");
                String key = s;
                key = ct(key);
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(key).create()));
                continue;
            }
            if (hasClick && s.startsWith("click:")){
                s = s.replace("click:", "");
                plugin.log("click: "+s);
                String[] var0 = s.split(":");
                String action = var0[0];
                String key = var0[1];
                key = key.replace("{http}", "http://");
                key = key.replace("{https}", "https://");
                ClickEvent.Action hover = ClickEvent.Action.valueOf(action.toUpperCase());
                component.setClickEvent(new ClickEvent(hover, key));
            }
        }

        return component;
    }


    public static String arrayJoin(String[] args) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i != args.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public TextComponent getMSG(CommandSender sender, String in){
        TextComponent out;
        ProxiedPlayer p = null;

        if (sender instanceof ProxiedPlayer){
            p = (ProxiedPlayer) sender;
        }

        boolean hasComponents = in.contains("{component}");

        if (hasComponents){
            String[] var = in.replace("{component}", "`").split("`");
            out = new TextComponent(getMSG(p, var[0]));

            TextComponent component = getComponent(var[1]);

            sender.sendMessage(out, component);
            return null;
        } else {
            in = plugin.getPlaceholders().apply(in);
            if (p!=null){
                in = in.replace("{rank}", plugin.getPrefix(p));
                in = in.replace("{luckperms_prefix}", plugin.getPrefix(p));
                in = in.replace("{player}", p.getName());
                if (p.getServer()!=null) {
                    in = in.replace("{server}", p.getServer().getInfo().getName());
                    in = in.replace("%server%", p.getServer().getInfo().getName());
                }
                in = in.replace("%rank%", plugin.getPrefix(p));
                in = in.replace("%luckperms_prefix%", plugin.getPrefix(p));
                in = in.replace("%player%", p.getName());
            }
            in = in.replace("{prefix}", plugin.getFileUtils().getMessages().getString("prefix"));
            if (in.contains("{center}")){
                in = in.replace("{center}", "");
                in = getCenteredMessage(in);
            }
        }
        out = new TextComponent(ct(in));

        return out;
    }

    public void sendMessage(CommandSender sender, String msg) {
        TextComponent component = new TextComponent();
        if (msg.contains(".") && !msg.contains(" ")){
            msg = plugin.getFileUtils().getMessages().getString(msg);
        }
        if (msg.contains("\\n")){
            String[] split = msg.split("\\n");
            sendMessage(sender, split);
            return;
        }

        if (msg.contains("\n")){
            String[] split = msg.split("\n");
            sendMessage(sender, split);
            return;
        }

        component = getMSG(sender, msg);
        if (component==null){
            return;
        }
        if (sender instanceof ProxiedPlayer){
            sender.sendMessage(TextComponent.fromLegacyText(component.getText()));
        } else {
            plugin.getProxy().getConsole().sendMessage(TextComponent.fromLegacyText(component.getText()));
        }
    }

    ScheduledTask task;
    int announce = 0;
    public void initTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        task = plugin.getProxy().getScheduler().schedule(plugin, () -> {
            if (!plugin.getConfig().getBoolean("announces.enabled", true)) {
                return;
            }
                    String mode = plugin.getConfig().getString("announces.mode");
                    List<String> motds = plugin.getConfig().getStringList("announces.list");
                    List<String> blacklist = new ArrayList<>(plugin.getConfig().getStringList("announces.blacklist-servers"));
                    String bypass = plugin.getConfig().getString("announces.bypass");
                    MOTDMode mmode;
                    try {
                        mmode = MOTDMode.valueOf(mode.toUpperCase());
                    } catch (Exception e) {
                        return;
                    }
                    String desc;
                    if (mmode == MOTDMode.SEQUENTIAL) {
                        if ((announce + 1) > motds.size()) {
                            announce = 0;
                        }
                        desc = motds.get(announce);
                        announce++;
                    } else {
                        desc = motds.get(new Random().nextInt(motds.size()));
                    }
                    boolean byp = false;
                    if (desc.contains("{bypass}")) {
                        byp = true;
                        desc = desc.replace("{bypass}", "");
                    }
                    for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                        if (byp && p.hasPermission(bypass)) {
                            continue;
                        }
                        if (blacklist.contains(p.getServer().getInfo().getName())) {
                            continue;
                        }
                        sendMessage(p, desc);
                    }
                }, 0L, plugin.getConfig().getInt("announces.delay"), TimeUnit.SECONDS);
    }

    public void sendMessage(CommandSender to, String... list){
        for (String s : list){
            sendMessage(to, s);
        }
    }
    public void sendMessage(Collection<ProxiedPlayer> to, String msg){
        for (ProxiedPlayer p : to){
            sendMessage(p, msg);
        }
    }

    public void executeActions(CommandSender sender, List<String> actions){
        TextComponent component = null;
        for (String s : actions){
            if (s.startsWith("[msg]")){
                s = s.replace("[msg]", "");
                component = getMSG(sender, s);
                if (component==null){
                    continue;
                }
                sender.sendMessage(component);
            }
        }
    }

    public void sendMSGtoStaff(ProxiedPlayer staff, String msg){
        TextComponent component = new TextComponent(getMSG(staff, msg));
        if (staff != null && staff.getServer() != null){
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ct("&fConectado en: &e"+ staff.getServer().getInfo().getName())).create()));
        }
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/sc "));
        for (ProxiedPlayer p : plugin.getProxy().getPlayers()){
            if (p.hasPermission(Permissions.STAFFCHAT_STAFF.get())){
                if (!Managers.isHided(p)){
                    p.sendMessage(component);
                }
            }
        }
        plugin.getProxy().getConsole().sendMessage(component);
    }

    public String colorize(String string) {
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        for (Matcher matcher = pattern.matcher(string); matcher.find(); matcher = pattern.matcher(string)) {
            String color = string.substring(matcher.start(), matcher.end());
            string = string.replace(color, String.valueOf(ChatColor.of(color.replace("&", ""))));
        }
        string = ChatColor.translateAlternateColorCodes('&', string);
        return string;
    }
}
