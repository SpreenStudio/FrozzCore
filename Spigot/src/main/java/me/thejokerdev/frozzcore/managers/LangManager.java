package me.thejokerdev.frozzcore.managers;

import lombok.Getter;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.api.utils.FileUtils;
import me.thejokerdev.frozzcore.type.Lang;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class LangManager {
    private final SpigotMain plugin;
    @Getter
    private LinkedHashMap<String, LinkedList<Lang>> languages;
    private HashMap<String, FileUtils> settings;
    @Getter
    private List<String> languageList;
    private final File langFolder;
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

    public LangManager init(){
        languages = new LinkedHashMap<>();
        settings = new LinkedHashMap<>();
        languageList = new ArrayList<>();


        if (!langFolder.exists()){
            langFolder.mkdir();
        }

        if (!getFromWeb()){
            error = "&cCan't download files from github repository.";
            return this;
        }

        if (Objects.requireNonNull(langFolder.listFiles()).length == 0){
            error = "&cAny file downloaded.";
            return this;
        }

        loadFiles();

        running = true;
        updated = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        return this;
    }

    public void loadFiles(){
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
                        File file = f2;
                        list.add(new Lang(file));
                        if (!languageList.contains(f2.getName().replace(".yml", ""))){
                            languageList.add(f2.getName().replace(".yml", ""));
                        }
                        langs +=1;
                    } else if (f2.getName().equals(".yml") && f2.getName().contains("module_settings")){
                        settings.put(var1, new FileUtils(f2));
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
        if (!getFromWeb()){
            error = "&cCan't download files from github repository.";
        }
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

}
