package me.thejokerdev.frozzcore.events;

import me.thejokerdev.frozzcore.BungeeMain;
import me.thejokerdev.frozzcore.enums.MOTDMode;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PingEvents implements Listener {
    int motd = 0;
    int favOrder = 0;
    private final BungeeMain plugin;

    public PingEvents(BungeeMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProxyPingEvent(ProxyPingEvent event) {
        MOTDMode mmode;
        String desc;
        if (!this.plugin.getConfig().getBoolean("motd.enabled"))
            return;
        int version = event.getConnection().getVersion();
        boolean supportHex = (version >= 754);
        List<String> faviconList = new ArrayList<>();
        Object favObject = this.plugin.getConfig().get("motd.favicon");
        String mode = this.plugin.getConfig().getString("motd.mode");
        int max_players = this.plugin.getConfig().getInt("motd.max-players", 1);
        List<String> motds = this.plugin.getConfig().getStringList("motd.list");
        if (supportHex && this.plugin.getConfig().get("motd.hex-list") != null)
            motds = this.plugin.getConfig().getStringList("motd.hex-list");
        if (this.plugin.getConfig().getBoolean("BungeeMaintenance.enabled")) {
            favObject = this.plugin.getConfig().get("BungeeMaintenance.favicon");
            motds = this.plugin.getConfig().getStringList("BungeeMaintenance.motds");
            if (supportHex && this.plugin.getConfig().get("BungeeMaintenance.motds-hex") != null)
                motds = this.plugin.getConfig().getStringList("BungeeMaintenance.motds-hex");
        }
        if (favObject instanceof List<?>) {
            List<?> list = (List<?>) favObject;
            for (Object fav : list) {
                faviconList.add((String) fav);
            }
        } else {
            faviconList.add((String) favObject);
        }
        try {
            mmode = MOTDMode.valueOf(mode.toUpperCase());
        } catch (Exception e) {
            return;
        }
        if (mmode == MOTDMode.SEQUENTIAL) {
            if (this.motd + 1 > motds.size())
                this.motd = 0;
            desc = motds.get(this.motd);
            this.motd++;
        } else {
            desc = motds.get((new Random()).nextInt(motds.size()));
        }
        ServerPing ping = event.getResponse();
        TextComponent textComponent = this.plugin.getUtils().getMSG(null, desc);
        if (supportHex) {
            textComponent = new TextComponent(TextComponent.fromLegacyText(this.plugin.getUtils().colorize(desc)));
        }
        ping.setDescriptionComponent(textComponent);
        ping.getPlayers().setMax(max_players);
        if (!faviconList.isEmpty()) {
            String favicon;
            if (mmode == MOTDMode.SEQUENTIAL) {
                if (this.favOrder + 1 > faviconList.size())
                    this.favOrder = 0;
                favicon = faviconList.get(this.favOrder);
                this.favOrder++;
            } else {
                favicon = faviconList.get((new Random()).nextInt(faviconList.size()));
            }
            BufferedImage img = getFavicon(favicon);
            if (img != null) {
                ping.setFavicon(Favicon.create(img));
            }
        }
        event.setResponse(ping);
    }

    public BufferedImage getFavicon(String path) {
        path = path.replace("{dataFolder}", plugin.getDataFolder().getAbsolutePath());
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        BufferedImage img;
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            plugin.getUtils().sendMessage(plugin.getProxy().getConsole(), "§cError loading favicon: " + e.getMessage());
            return null;
        }
        return img;
    }
}
