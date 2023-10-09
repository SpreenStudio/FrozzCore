package me.thejokerdev.frozzcore.data;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.thejokerdev.frozzcore.SpigotMain;
import me.thejokerdev.frozzcore.enums.DataType;
import me.thejokerdev.frozzcore.enums.ModifierStatus;
import me.thejokerdev.frozzcore.enums.VisibilityType;
import me.thejokerdev.frozzcore.type.Data;
import me.thejokerdev.frozzcore.type.FUser;
import me.thejokerdev.frozzcore.type.NickData;
import org.bson.Document;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;

public class MongoDB extends Data {

    private MongoCollection<Document> collection;
    private MongoDatabase db;
    private MongoClient client;

    private final boolean running = false;

    public MongoDB(SpigotMain plugin) {
        super(plugin);
    }

    @Override
    public DataType getType() {
        return DataType.MONGODB;
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public void syncData(FUser var) {
        Document query = new Document("_id", var.getUniqueID().toString());
        Document found = collection.find(query).first();

        if (found == null){
            Document document = new Document("_id", var.getUniqueID().toString());
            document.put("name", var.getName());
            document.put("lang", plugin.getClassManager().getLangManager().getDefault());
            document.put("visibility", VisibilityType.ALL.name());
            document.put("firstJoin", true);
            document.put("hype", 0);
            document.put("jump", ModifierStatus.OFF.name());
            document.put("doubleJump", ModifierStatus.OFF.name());
            document.put("fly", ModifierStatus.OFF.name());
            document.put("speed", ModifierStatus.OFF.name());
            document.put("money", 0.0d);
            document.put("nicked", false);
            document.put("joinDate", new Date().getTime());
            collection.insertOne(document);
            return;
        }

        Document document = new Document("_id", var.getUniqueID().toString());
        document.put("name", var.getName());
        document.put("lang", var.getLang());
        document.put("visibility", var.getVisibilityType().name());
        document.put("firstJoin", false);
        document.put("hype", var.getHype());
        document.put("jump", var.getJump().name());
        document.put("doubleJump", var.getDoubleJump().name());
        document.put("fly", var.getAllowFlight().name());
        document.put("speed", var.getSpeed().name());
        document.put("money", var.getMoney());
        document.put("nicked", var.isNicked());
        if (var.isNicked()) {
            document.put("nickData", var.getNickData().serialize());
        } else {
            document.put("nickData", null);
        }
        document.put("joinDate", var.getJoinDate() != null ? var.getJoinDate().getTime() : new Date().getTime());
        collection.replaceOne(found, document);
    }

    private HashMap<String, Integer> tries;

    @Override
    public void getData(FUser var) {
        Document query = new Document("_id", var.getUniqueID().toString());
        Document found = collection.find(query).first();

        if (found == null){
            Document document = new Document("_id", var.getUniqueID().toString());
            document.put("name", var.getName());
            document.put("lang", plugin.getClassManager().getLangManager().getDefault());
            document.put("visibility", VisibilityType.ALL.name());
            document.put("firstJoin", true);
            document.put("hype", 0);
            document.put("jump", ModifierStatus.OFF.name());
            document.put("doubleJump", ModifierStatus.OFF.name());
            document.put("fly", ModifierStatus.OFF.name());
            document.put("speed", ModifierStatus.OFF.name());
            document.put("money", 0.0d);
            document.put("nicked", false);
            document.put("joinDate", new Date().getTime());
            collection.insertOne(document);
            return;
        }

        var.setLang(found.getString("lang"));
        var.setVisibilityType(VisibilityType.valueOf(found.getString("visibility").toUpperCase()));
        var.setFirstJoin(found.getBoolean("firstJoin"));
        var.setHype(found.getInteger("hype"));
        var.setMoney(found.getDouble("money") == null ? 0.0d : found.getDouble("money"));
        ModifierStatus jump = ModifierStatus.OFF;
        ModifierStatus doubleJump = ModifierStatus.OFF;
        ModifierStatus allowFlight = ModifierStatus.OFF;
        ModifierStatus speed = ModifierStatus.OFF;
        try {
            jump = ModifierStatus.valueOf(found.getString("jump").toUpperCase());
            doubleJump = ModifierStatus.valueOf(found.getString("doubleJump").toUpperCase());
            allowFlight = ModifierStatus.valueOf(found.getString("fly").toUpperCase());
            speed = ModifierStatus.valueOf(found.getString("speed").toUpperCase());
            var.set(jump, doubleJump, allowFlight, speed);
        } catch (ClassCastException ignored){
        }
        if (plugin.isNickAPIEnabled() && found.getBoolean("nicked", false)){
            plugin.debug("{prefix}&7Loading nick data for &e" + var.getName() + "&7... #1");
            var.setNickData(new NickData(plugin, var, found.getString("nickData")));
            var.setNicked(true);
        }

        if (found.getLong("joinDate") == null){
            found.put("joinDate", new Date().getTime());
        } else {
            var.setJoinDate(new Date(found.getLong("joinDate")));
        }
    }

    @Override
    public void reload() {
        close();
        setup();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void close() {
        client.close();
    }

    @Override
    public void setup() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("data.mongodb");
        boolean useURI = !section.getString("uri", "mongodb://localhost:27017").equals("mongodb://localhost:27017");
        if (useURI) {
            MongoClientURI uri = new MongoClientURI(section.getString("uri"));
            client = new MongoClient(uri);
        } else {
            String host = section.getString("host");
            int port = 27017;
            String[] split = host.split(":");
            if (split.length == 2) {
                host = split[0];
                port = Integer.parseInt(split[1]);
            }
            ServerAddress address = new ServerAddress(host, port);

            String password = section.getString("password");
            if (password != null && !password.equalsIgnoreCase("")){
                MongoCredential credential = MongoCredential.createCredential(section.getString("user"), section.getString("database"), password.toCharArray());
                client = new MongoClient(address, credential, MongoClientOptions.builder().build());
            } else {
                client = new MongoClient(address);
            }
        }
        db = client.getDatabase(section.getString("database"));
        collection = db.getCollection("core");

        plugin.debug("{prefix}&7Connected to database.");
    }
}
