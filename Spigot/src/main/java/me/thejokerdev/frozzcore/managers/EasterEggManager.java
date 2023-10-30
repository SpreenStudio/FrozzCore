package me.thejokerdev.frozzcore.managers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.data.MongoDB;
import me.thejokerdev.frozzcore.type.Data;
import org.bson.Document;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import studio.spreen.cloud.api.CloudAPI;

import java.util.*;

public class EasterEggManager {
    private MongoCollection<Document> collection;
    private Map<String, List<String>> easter_eggs = new HashMap<>();
    private final String server;
    private final SpigotMain plugin;

    public EasterEggManager(SpigotMain plugin) {
        this.plugin = plugin;
        this.server = CloudAPI.getBukkitAPI().getThisServer().getGroup().getName();

        if(plugin.getClassManager() == null){
            plugin.getLogger().warning("getClassManager ES NULLLLLLLLL");
        }

        Data data = plugin.getClassManager().getDataManager().getData();

        if (!(data instanceof MongoDB))
            return;

        MongoDatabase db = ((MongoDB) data).getDatabase();
        collection = db.getCollection("eastereggs");
        easter_eggs = getAllEasterEggsMap();

        for(Chunk chunk : Bukkit.getWorlds().get(0).getLoadedChunks()){
            onLoadChunk(chunk);
        }
    }

    public void addEasterEgg(Location location) {
        String loc = getLocationString(location);
        Document document = getServerDocument();
        List<String> eggsList = easter_eggs.getOrDefault(server, new ArrayList<>());

        if (document != null) {
            List<String> eggs = document.getList("eggs", String.class);
            eggs.add(loc);
            updateEggsList(eggs);
        } else {
            Document newDocument = createEasterEggDocument(Collections.singletonList(loc));
            insertEasterEggDocument(newDocument);
        }

        eggsList.add(loc);
        easter_eggs.put(server, eggsList);
        placePlayerHeadWithTextureValue(location, "ewogICJ0aW1lc3RhbXAiIDogMTY5ODU5MDQxNjkxNCwKICAicHJvZmlsZUlkIiA6ICI4ZGUyNDAzYTEyMjU0ZmFkOTM1OTYxYWFlYmQwNGUyOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJZdW5hbWkyNyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83NjMyODQ4NWM4ODA1NzUzMDdkYmIwMjMzODdmYjY1MTQwMmNjZDg2MDc1YzgwNmM4NTgzNWZlMzYyMzc0ODgiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==");
    }

    public boolean checkEasterEgg(Location location) {
        String loc = getLocationString(location);
        List<String> eggsList = easter_eggs.getOrDefault(server, new ArrayList<>());
        return eggsList != null && eggsList.contains(loc);
    }

    public void addEaster(Player player, Location location) {
        if (!checkEasterEgg(location)) {
            player.sendMessage("no es el huevo");
            return;
        }

        String loc = getLocationString(location);
        Document document = getPlayerDocument(player.getUniqueId());
        List<String> playerEggs = getPlayerEggsList(document);

        if (playerEggs.contains(loc)) {
            player.sendMessage("Ya tienes este EasterEgg");
            return;
        }

        playerEggs.add(loc);

        if (document != null) {
            updatePlayerEggsList(player, playerEggs);
        } else {
            Document newDocument = createPlayerEasterEggDocument(player.getUniqueId().toString(), playerEggs);
            insertPlayerEasterEggDocument(newDocument);
        }
        player.sendMessage("encontraste el easteregg "+getEasterEggsFoundByPlayer(player.getUniqueId())+" - "+getTotalEasterEggsInAllServers());
    }

    public void removeEasterEgg(Location location) {
        String loc = getLocationString(location);
        Document document = getServerDocument();
        List<String> eggsList = easter_eggs.getOrDefault(server, new ArrayList<>());

        if (document != null) {
            List<String> eggs = document.getList("eggs", String.class);
            if (eggs.remove(loc)) {
                updateEggsList(eggs);
            }
        }
        eggsList.remove(loc);
        easter_eggs.put(server, eggsList);
    }

    public int getEasterEggsFoundByPlayer(UUID uuid) {
        Document playerDocument = getPlayerDocument(uuid);
        List<String> playerEggs = getPlayerEggsList(playerDocument);
        return playerEggs.size();
    }

    public int getTotalEasterEggsInAllServers() {
       int amount = 0;
       for(List<String> list : easter_eggs.values()){
           amount += list.size();
       }

       return amount;
    }

    public void removeAllEasterEggsForPlayer(UUID uuid) {
        Document playerDocument = getPlayerDocument(uuid);

        if (playerDocument != null) {
            playerDocument.remove("player_eggs");
            collection.replaceOne(Filters.eq("uuid", uuid.toString()), playerDocument);
        }
    }

    public Map<String, List<String>> getAllEasterEggsMap() {
        Map<String, List<String>> easterEggsByServer = new HashMap<>();

        for (Document serverDocument : collection.find()) {
            if(!serverDocument.containsKey("server"))
                continue;

            String serverName = serverDocument.getString("server");
            List<String> serverEggs = serverDocument.getList("eggs", String.class);

            easterEggsByServer.put(serverName, serverEggs);
        }

        return easterEggsByServer;
    }

    private String getLocationString(Location location) {
        return location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }

    private Location getLocation(String location){
        String[] data = location.split("_");
        int x = Integer.parseInt(data[1]);
        int y = Integer.parseInt(data[2]);
        int z = Integer.parseInt(data[3]);

        return new Location(Bukkit.getWorld(data[0]), x, y, z);
    }

    private Document getServerDocument() {
        return collection.find(Filters.eq("server", server)).first();
    }

    private void updateEggsList(List<String> eggs) {
        collection.updateOne(Filters.eq("server", server), Updates.set("eggs", eggs));
    }

    private Document createEasterEggDocument(List<String> eggs) {
        return new Document("server", server).append("eggs", eggs);
    }

    private void insertEasterEggDocument(Document document) {
        collection.insertOne(document);
    }

    private Document getPlayerDocument(UUID uuid) {
        return collection.find(Filters.eq("uuid", uuid.toString())).first();
    }

    private List<String> getPlayerEggsList(Document document) {
        if (document != null) {
            return document.getList("player_eggs", String.class) == null ? new ArrayList<>() : document.getList("player_eggs", String.class);
        } else {
            return new ArrayList<>();
        }
    }

    private void updatePlayerEggsList(Player player, List<String> playerEggs) {
        collection.updateOne(Filters.eq("uuid", player.getUniqueId().toString()), Updates.set("player_eggs", playerEggs));
    }

    private Document createPlayerEasterEggDocument(String uuid, List<String> playerEggs) {
        return new Document("uuid", uuid).append("player_eggs", playerEggs);
    }

    private void insertPlayerEasterEggDocument(Document document) {
        collection.insertOne(document);
    }

    public void onLoadChunk(Chunk chunk){
        List<String> eggs = easter_eggs.get(server);
        if(eggs == null)
            return;

        for(String loc : eggs){
            Location location = getLocation(loc);
            if(!isLocationInChunk(chunk, location))
                continue;

            placePlayerHeadWithTextureValue(location, "ewogICJ0aW1lc3RhbXAiIDogMTY5ODU5MDQxNjkxNCwKICAicHJvZmlsZUlkIiA6ICI4ZGUyNDAzYTEyMjU0ZmFkOTM1OTYxYWFlYmQwNGUyOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJZdW5hbWkyNyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83NjMyODQ4NWM4ODA1NzUzMDdkYmIwMjMzODdmYjY1MTQwMmNjZDg2MDc1YzgwNmM4NTgzNWZlMzYyMzc0ODgiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==");
        }
    }

    private boolean isLocationInChunk(Chunk chunk, Location location) {
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        int blockX = location.getBlockX();
        int blockZ = location.getBlockZ();

        return blockX >> 4 == chunkX && blockZ >> 4 == chunkZ;
    }

    public void placePlayerHeadWithTextureValue(Location location, String texture) {
        Block block = location.getBlock();
        if (block.getType() != Material.SKULL)
            block.setType(Material.SKULL);

        Skull skull = (Skull)block.getState();
        skull.setSkullType(SkullType.PLAYER);

        skull.setSkullTexture(texture);
    }
}
