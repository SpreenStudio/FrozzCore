package me.thejokerdev.frozzcore.menus;

import com.cryptomorin.xseries.XSound;
import me.clip.placeholderapi.PlaceholderAPI;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.enums.ItemType;
import me.thejokerdev.frozzcore.type.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import studio.spreen.cloud.api.objects.ServerObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LanguageMenu extends Menu {
    private int page = 0;
    private int pages = 0;
    private Button prevPage;
    private Button nextPage;

    public LanguageMenu(SpigotMain plugin, Player player){
        super(plugin, player, "languages", true);

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
        Lang lang = langObjectsMap.get(item);
        if (lang==null) return;
        if (lang.getId().equals(getUser().getLang())){
            plugin.getUtils().sendMessage(getPlayer(), "general@messages.alreadySelected");
            XSound.ENTITY_ENDERMAN_TELEPORT.play(getPlayer(), 1, 0.4f);
            return;
        }
        getUser().setLang(lang.getId(), false, true);
    }

    @Override
    public void update() {
        boolean clear = getConfig().getBoolean("settings.clear", false);
        List<Integer> notUsed = new ArrayList<>();

        FUser fUser = plugin.getClassManager().getPlayerManager().getUser(getPlayer());
        for (Button b : buttons){
            if (!b.canView()){
                continue;
            }
            setItem(b);
            notUsed.addAll(b.getSlot());
        }

        List<Lang> languages = getSortedLanguages();

        int maxItems = 7;
        List<Integer> multiply = new ArrayList<>(Arrays.asList(36, 45, 54));
        int size = getInventory().getSize();
        for (int i : multiply){
            if (size > i){
                maxItems += 7;
            }
        }

        if (page > 0 && languages.size() < page * maxItems+1){
            page = 0;
            update();
            return;
        }

        pages = (int) Math.ceil((double) languages.size() / maxItems);
        if (page > 0){
            setItem(prevPage.getSlot(), previousPage());
            notUsed.add(45);
        }
        if (languages.size() > (page + 1) * maxItems){
            setItem(nextPage.getSlot(), nextPage());
            notUsed.add(53);
        }
        if (languages.size() > maxItems){
            languages = languages.subList(page*maxItems, Math.min(page*maxItems+maxItems, languages.size()));
        }
        if (!languages.isEmpty()){
            for (int i = 0; i < languages.size(); i++){
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
                Lang language = languages.get(i);
                SimpleItem item = getLangItem(fUser, language);
                ItemStack stack;
                langObjectsMap.put((stack = item.build(getPlayer())), language);
                setItem(slot, stack);
                notUsed.add(slot);
            }
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

    private List<Lang> getSortedLanguages() {
        List<Lang> languages = new ArrayList<>(plugin.getClassManager().getLangManager().getLangs());
        languages.sort((o1, o2) -> {
            // Alphabetical order
            int i = o1.getId().compareTo(o2.getId());
            if (i == 0){
                // If the same, order by last update
                return o1.getLastUpdate().compareTo(o2.getLastUpdate());
            }
            return i;
        });
        return languages;
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
        FUser fUser = plugin.getClassManager().getPlayerManager().getUser(getPlayer());
        if (getConfig().get("extra-items") != null){
            for (String key : getConfig().getSection("extra-items").getKeys(false)){
                key = "extra-items."+key;
                buttons.add(createButtonBySection(fUser, key));
            }
        }
        prevPage = createButtonBySection(fUser, "items.prevPage");
        nextPage = createButtonBySection(fUser, "items.nextPage");
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

    private final HashMap<ItemStack, Lang> langObjectsMap = new HashMap<>();
    private final HashMap<String, Button> langItemsMap = new HashMap<>();

    public Button loadLangItem(FUser fUser, String lang){
        if (getConfig().get("items."+lang)==null){
            return null;
        }
        return createButtonBySection(fUser, "items."+lang);
    }

    private SimpleItem getLangItem(FUser fUser, Lang lang){

        Button button = langItemsMap.computeIfAbsent(lang.getId(), s -> loadLangItem(fUser, s));
        if (button == null){
            button = loadLangItem(fUser, "no-data");
        }

        SimpleItem item = button.getItem().clone();
        item.setDisplayName(applyPlaceHolders(item.getDisplayName(), lang));
        item.setLore(applyPlaceHolders(item.getLore(), lang));
        item.setInfo(lang.getId());
        return item;
    }

    public String applyPlaceHolders(String in, Lang lang){
        in = in.replace("{actualPage}", String.valueOf(page+1));
        in = in.replace("{maxPages}", String.valueOf(pages));

        if (lang != null) {
            try {
                in = in.replace("{lang}", lang.getId());
                in = in.replace("{language}", lang.getId());
                in = in.replace("{translated}", plugin.getClassManager().getLangManager().translatedPercent(lang));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return in;
    }

    private Button createButtonBySection(FUser fUser, String sectionPath) {
        return new Button(fUser, getConfig(), sectionPath, ItemType.MENU, getMenuId());
    }

    public String applyPlaceHolders(String in){
        return applyPlaceHolders(in, null);
    }

    public List<String> applyPlaceHolders(List<String> in, Lang lang){
        List<String> out = new ArrayList<>(in);
        out.replaceAll(s -> applyPlaceHolders(s, lang));
        return out;
    }

    public List<String> applyPlaceHolders(List<String> in){
        return applyPlaceHolders(in, null);
    }

}
