package me.thejokerdev.frozzcore.type;

import com.cryptomorin.xseries.messages.ActionBar;
import lombok.Getter;
import lombok.Setter;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.api.events.PlayerNickEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import studio.spreen.statsspigot.StatsSpigot;
import xyz.haoshoku.nick.api.NickAPI;

import java.util.concurrent.CompletableFuture;

@Getter @Setter
public class NickData {
    public final SpigotMain plugin;
    private final FUser user;
    private String name;
    private String skin;
    private String rank;
    private String signature;

    private String originalSkin;

    private BukkitTask nickTask;

    public NickData(SpigotMain plugin, FUser user, String info) {
        this.plugin = plugin;
        this.user = user;
        if (info.contains(";")) {
            String[] data = info.split(";");
            this.name = data[0];
            this.rank = data[1];
        } else {
            this.name = info;
        }
        loadSkin();
        plugin.debug("NickData created from info: " + info);
    }

    public void loadSkin(){
        OfflinePlayer player = SpigotMain.getPlugin().getServer().getOfflinePlayer(name);
        String[] skinData = NickAPI.getSkinFetcher().getSkinDataByUUID(player.getUniqueId());
        skin = skinData[0];
        signature = skinData[1];
    }

    public void resetSkin() {
        Player player = user.getPlayer();
        NickAPI.resetNick(player);
        NickAPI.resetSkin(player);
        NickAPI.resetUniqueId(player);
        NickAPI.resetGameProfileName(player);
        NickAPI.refreshPlayer(player);

        boolean isGame = StatsSpigot.INSTANCE.getConfig().isGameServer();

        if (!isGame) return;

        stopNickTask();
    }

    private String originalName;

    public void applySkin() {
        Player player = user.getPlayer();
        if (originalName == null) {
            originalName = player.getName();
        }
        String name = getName();
        NickAPI.nick(player, name);
        NickAPI.setSkin(player, name);
        NickAPI.setUniqueId(player, name);
        NickAPI.setGameProfileName(player, name);
        NickAPI.refreshPlayer(player);

        player.setPlayerListName(name);

        boolean isGame = StatsSpigot.INSTANCE.getConfig().isGameServer();

        if (!isGame) return;
        startNickTask();
    }

    public void startNickTask(){
        if (nickTask != null){
            nickTask.cancel();
        }
        nickTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (user.getPlayer() == null) {
                    nickTask.cancel();
                    nickTask = null;
                    return;
                }
                ActionBar.sendActionBar(user.getPlayer(), user.getMSG("key:nicked.actionbar"));
            }
        }.runTaskTimer(SpigotMain.getPlugin(), 0L, 10L);
    }

    public void stopNickTask() {
        if (nickTask != null) {
            nickTask.cancel();
            nickTask = null;
        }
        ActionBar.sendActionBar(user.getPlayer(), "");
    }

    public void setNick(){
        PlayerNickEvent event = new PlayerNickEvent(user, PlayerNickEvent.Cause.NICK);
        SpigotMain.getPlugin().getServer().getPluginManager().callEvent(event);
    }

    public boolean isValid(){
        String regex = "[a-zA-Z0-9_]{3,16}";
        return name.matches(regex);
    }

    public String serialize(){
        return name + ";" + rank;
    }

    public void loadAndApplySkin() {
        CompletableFuture.runAsync(this::loadSkin).whenCompleteAsync((aVoid, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }
            applySkin();
        });
    }
}
