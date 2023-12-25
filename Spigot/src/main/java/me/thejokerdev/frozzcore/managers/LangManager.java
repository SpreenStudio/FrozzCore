package me.thejokerdev.frozzcore.managers;

import lombok.Getter;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.api.utils.FileUtils;
import me.thejokerdev.frozzcore.enums.LanguageType;
import me.thejokerdev.frozzcore.type.Lang;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class LangManager {
    private final SpigotMain plugin;
    @Getter
    private LinkedHashMap<String, LinkedList<Lang>> languages;

    @Getter
    private LinkedHashMap<String, LinkedList<Lang>> internalLanguages;
    private HashMap<String, FileUtils> settings;
    @Getter
    private List<String> languageList;
    private File langFolder;
    @Getter
    private boolean running = false;
    @Getter
    private String error = "N/A";
    @Getter
    private int langs = 0;
    @Getter
    private String updated;

    public LangManager(SpigotMain plugin) {
        this.plugin = plugin;
        langFolder = new File(plugin.getDataFolder()+"/lang/");
    }

    public void init(){
        LanguageType languageType = LanguageType.REMOTE;
        try {
            languageType = LanguageType.valueOf(plugin.getConfig().getString("settings.languages.type"));
        } catch (Exception ignored){
        }
        if (languageType == LanguageType.LOCAL){
            langFolder = new File(plugin.getConfig().getString("settings.languages.path", "/root/storage/languages/"));
        } else {
            langFolder = new File(plugin.getDataFolder()+"/lang/");
        }
        languages = new LinkedHashMap<>();
        settings = new LinkedHashMap<>();
        languageList = new ArrayList<>();


        if (!langFolder.exists()){
            langFolder.mkdir();
        }

        if (languageType == LanguageType.REMOTE || langFolder.listFiles().length == 0) {
            if (!getFromWeb()) {
                error = "&cCan't download files from github repository.";
                return;
            }
        }

        if (Objects.requireNonNull(langFolder.listFiles()).length == 0){
            error = "&cAny file downloaded.";
            return;
        }

        loadFiles();

        running = true;
        updated = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
    }

    public void loadFiles(){
        internalLanguages = new LinkedHashMap<>();
        languages.clear();
        languageList.clear();
        for (File f : Objects.requireNonNull(langFolder.listFiles())){
            if (f.isDirectory()){
                LinkedList<Lang> list = new LinkedList<>();
                String var1 = f.getName();
                if (Objects.requireNonNull(f.listFiles()).length == 0){
                    continue;
                }
                for (File f2 : Objects.requireNonNull(f.listFiles())){
                    if (f2.getName().endsWith(".yml") && f2.getName().contains("_")){
                        String id = f2.getName().replace(".yml", "");
                        Lang lang = new Lang(f2);
                        list.add(lang);

                        LinkedList<Lang> list1 = new LinkedList<>();
                        if (internalLanguages.containsKey(id)){
                            list1.addAll(internalLanguages.get(id));
                        }
                        list1.add(lang);
                        internalLanguages.put(id, list1);
                        if (!languageList.contains(f2.getName().replace(".yml", ""))){
                            languageList.add(f2.getName().replace(".yml", ""));
                        }
                        langs +=1;
                    }
                }
                languages.put(var1, list);
            }
        }
    }

    public String getDefault(){
        return plugin.getConfig().getString("settings.languages.default");
    }

    public boolean getFromWeb(){
        boolean b = false;
        for (String s : plugin.getConfig().getStringList("settings.languages.downloader.folders")){
            b = plugin.getClassManager().getLangDownloader().downloadFromGitHub(s);
        }
        return b;
    }

    public void reload(){
        this.init();
    }

    public Lang getLanguageOfSection(String section, String lang) {
        if (getSection(section) == null || getSection(section).isEmpty()){
            return null;
        }
        for (Lang lang1 : getSection(section)){
            if (lang1.getId().equalsIgnoreCase(lang)){
                return lang1;
            }
        }
        return null;
    }

    public LinkedList<Lang> getSection(String id){
        return languages.get(id);
    }

    public String translatedPercent(Lang lang){
        LinkedList<Lang> defaultLangs = new LinkedList<>(internalLanguages.get(getDefault()));

        int keys = 0;

        for (Lang lang1 : defaultLangs){
            keys += lang1.getFile().getKeys(true).size();
        }

        int translated = 0;
        defaultLangs.clear();
        defaultLangs.addAll(internalLanguages.get(lang.getId()));

        for (Lang lang1 : defaultLangs){
            translated += lang1.getFile().getKeys(true).size();
        }

        double percent = (translated * 100) / keys;

        return String.format("%.0f", percent)+"%";
    }

    public List<Lang> getLangs(){
        List<Lang> list = new ArrayList<>();
        for (LinkedList<Lang> list1 : internalLanguages.values()){
            list.add(list1.get(0));
        }
        return list;
    }

}
