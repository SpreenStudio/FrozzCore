package me.thejokerdev.frozzcore.menus;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import me.clip.placeholderapi.PlaceholderAPI;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.enums.ItemType;
import me.thejokerdev.frozzcore.type.Button;
import me.thejokerdev.frozzcore.type.Menu;
import me.thejokerdev.frozzcore.type.SimpleItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import studio.spreen.cloud.api.objects.ServerObject;

import java.util.*;

public class LobbyMenu extends Menu {
    private int page = 0;
    private int pages = 0;
    private Button prevPage;
    private Button nextPage;
    private Button noLobbies;
    private Button actualLobby;
    private Button lobbyToConnect;

    public LobbyMenu(SpigotMain plugin, Player player){
        super(plugin, player, "lobbies", true);

        updateLang();
        update();
    }
    @Override
    public void onOpen(InventoryOpenEvent var1) {
        update();
        if (getConfig().get("settings.update")==null){
            return;
        }
        int delay = getConfig().getInt("settings.update");
        if (delay == -1){
            return;
        }
        if (task != null){
            task.cancel();
            task = null;
        }
        task = new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimerAsynchronously(plugin, 0L, delay);
    }

    @Override
    public void onClose(InventoryCloseEvent var1) {
        if (task != null){
            task.cancel();
            task = null;
        }
        page = 0;
    }

    @Override
    public void onClick(InventoryClickEvent var1) {
        for (Button b : buttons){
            if ((b.getSlot().contains(-1) || b.getSlot().contains(var1.getSlot())) && plugin.getClassManager().getUtils().compareItems(var1.getCurrentItem(), b.getItem().build(getPlayer()))){
                if (!b.canView()){
                    continue;
                }
                if (!b.executeItemInMenuActions(var1)){
                    return;
                }
            }
        }

        ItemStack item = var1.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (prevPage.getSlot().contains(var1.getSlot())) {
            if (page == 0) return;
            page--;
            update();
            XSound.ITEM_BOOK_PAGE_TURN.play(getPlayer(), 1, 1f);
            return;
        }

        if (nextPage.getSlot().contains(var1.getSlot())) {
            page++;
            update();
            XSound.ITEM_BOOK_PAGE_TURN.play(getPlayer(), 1, 1f);
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        ServerObject server = serverObjectHashMap.get(item);
        if (server==null) return;
        if (server == plugin.getServerManager().getActualServer()){
            plugin.getUtils().sendMessage(getPlayer(), "general@messages.alreadyConnected");
            XSound.ENTITY_ENDERMAN_TELEPORT.play(getPlayer(), 1, 0.4f);
            return;
        }
        plugin.getUtils().sendPlayer(getPlayer(), server.getName());
    }

    @Override
    public void update() {
        boolean clear = getConfig().getBoolean("settings.clear", false);
        List<Integer> notUsed = new ArrayList<>();

        for (Button b : buttons){
            if (!b.canView()){
                continue;
            }
            setItem(b);
            notUsed.addAll(b.getSlot());
        }

        List<ServerObject> servers = new ArrayList<>();
        for (ServerObject object : plugin.getServerManager().getActualGroup().getServers()){
            if (object.getState().equalsIgnoreCase("MAINTENANCE") || object.getState().equalsIgnoreCase("STARTING")){
                continue;
            }
            servers.add(object);
        }

        servers.sort((o1, o2) -> {
            int i1 = getNumber(o1);
            int i2 = getNumber(o2);

            return Integer.compare(i1, i2);
        });

        int maxItems = 7;
        List<Integer> multiply = new ArrayList<>(Arrays.asList(36, 45, 54));
        int size = getInventory().getSize();
        for (int i : multiply){
            if (size > i){
                maxItems += 7;
            }
        }

        if (page > 0 && servers.size() < page*maxItems+1){
            page = 0;
            update();
            return;
        }

        pages = (int) Math.ceil((double) servers.size() /maxItems);
        if (page > 0){
            setItem(prevPage.getSlot(), previousPage());
            notUsed.add(45);
        }
        if (servers.size() > (page+1)*maxItems){
            setItem(nextPage.getSlot(), nextPage());
            notUsed.add(53);
        }
        if (servers.size() > maxItems){
            servers = servers.subList(page*maxItems, Math.min(page*maxItems+maxItems, servers.size()));
        }
        if (servers.size() > 0){
            for (int i = 0; i < servers.size(); i++){
                int slot = 10+i;
                if (slot >= 17){
                    slot += 2;
                }
                if (slot >= 26){
                    slot += 2;
                }
                if (slot >= 35){
                    slot += 2;
                }
                ServerObject p = servers.get(i);
                SimpleItem item = getServerItem(p);
                ItemStack stack;
                if (item == null){
                    continue;
                }
                serverObjectHashMap.put((stack = item.build(getPlayer())), p);
                setItem(slot, stack);
                notUsed.add(slot);
            }
        } else {
            setItem(noLobbies.getSlot(), noLobbies());
            notUsed.add(22);
        }

        if (clear) {
            for (int i = 0; i < getInventory().getSize(); i++) {
                if (notUsed.contains(i)) {
                    continue;
                }
                setItem(i, new ItemStack(Material.AIR));
            }
        }
    }

    public int getNumber(ServerObject server){
        String str= server.getName();
        int i = 1;
        try {
            String[] split = str.split("-");
            i = Integer.parseInt(split[split.length-1]);
        } catch (NumberFormatException ignored) {
        }
        return i;
    }

    @Override
    public void updateLang() {
        String title = getConfig().getString("settings.title");
        setTitle(PlaceholderAPI.setPlaceholders(getPlayer(), title));
        buttons.clear();
        if (getConfig().get("extra-items")!=null){
            for (String key : getConfig().getSection("extra-items").getKeys(false)){
                key = "extra-items."+key;
                buttons.add(new Button(plugin.getClassManager().getPlayerManager().getUser(getPlayer()), getConfig(), key, ItemType.MENU, getMenuId()));
            }
        }
        prevPage = new Button(plugin.getClassManager().getPlayerManager().getUser(getPlayer()), getConfig(), "items.prevPage", ItemType.MENU, getMenuId());
        nextPage = new Button(plugin.getClassManager().getPlayerManager().getUser(getPlayer()), getConfig(), "items.nextPage", ItemType.MENU, getMenuId());
        noLobbies = new Button(plugin.getClassManager().getPlayerManager().getUser(getPlayer()), getConfig(), "items.noLobbies", ItemType.MENU, getMenuId());
        actualLobby = new Button(plugin.getClassManager().getPlayerManager().getUser(getPlayer()), getConfig(), "items.actualLobby", ItemType.MENU, getMenuId());
        lobbyToConnect = new Button(plugin.getClassManager().getPlayerManager().getUser(getPlayer()), getConfig(), "items.lobbyToConnect", ItemType.MENU, getMenuId());
    }

    private SimpleItem nextPage(){
        SimpleItem item = nextPage.getItem().clone();
        item.setDisplayName(applyPlaceHolders(item.getDisplayName()));
        item.setLore(applyPlaceHolders(item.getLore()));
        return item;
    }

    private SimpleItem previousPage(){
        SimpleItem item = prevPage.getItem().clone();
        item.setDisplayName(applyPlaceHolders(item.getDisplayName()));
        item.setLore(applyPlaceHolders(item.getLore()));
        return item;
    }

    private SimpleItem noLobbies(){
        SimpleItem item = noLobbies.getItem().clone();
        item.setDisplayName(applyPlaceHolders(item.getDisplayName()));
        item.setLore(applyPlaceHolders(item.getLore()));
        return item;
    }

    private HashMap<ItemStack, ServerObject> serverObjectHashMap = new HashMap<>();

    private SimpleItem getServerItem(ServerObject server){
        SimpleItem item = server == plugin.getServerManager().getActualServer() ? actualLobby.getItem().clone() : lobbyToConnect.getItem().clone();
        item.setDisplayName(applyPlaceHolders(item.getDisplayName(), server));
        item.setLore(applyPlaceHolders(item.getLore(), server));
        item.setInfo(server.getName());
        item.setAmount(getNumber(server));
        return item;
    }

    public String applyPlaceHolders(String in, ServerObject server){
        in = in.replace("{actualPage}", String.valueOf(page+1));
        in = in.replace("{maxPages}", String.valueOf(pages));

        if (server != null) {
            try {
                in = in.replace("{serverName}", server.getName());
                String server_number = server.getName();
                if (server_number.contains("-")) {
                    try {
                        String[] split = server_number.split("-");
                        server_number = split[split.length - 1];
                        int i = Integer.parseInt(server_number);
                    } catch (NumberFormatException ignored) {
                    }
                }
                in = in.replace("{serverNumber}", server_number);
                in = in.replace("{serverState}", server.getState());
                in = in.replace("{serverOnline}", String.valueOf(server.getOnlinePlayerCount()));
                in = in.replace("{serverMax}", String.valueOf(server.getMaxPlayerCount()));
                in = in.replace("{serverMotd}", server.getMotd());
                in = in.replace("{serverMap}", server.getMap());
                in = in.replace("{serverGroup}", server.getGroup().getName());
                in = in.replace("{serverId}", server.getId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return in;
    }

    public String applyPlaceHolders(String in){
        return applyPlaceHolders(in, null);
    }

    public List<String> applyPlaceHolders(List<String> in, ServerObject server){
        List<String> out = new ArrayList<>(in);
        out.replaceAll(s -> applyPlaceHolders(s, server));
        return out;
    }

    public List<String> applyPlaceHolders(List<String> in){
        return applyPlaceHolders(in, null);
    }

}
