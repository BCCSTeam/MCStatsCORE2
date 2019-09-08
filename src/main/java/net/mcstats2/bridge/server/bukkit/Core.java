package net.mcstats2.bridge.server.bukkit;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSServer.MCSBukkitServer;
import net.mcstats2.core.api.MySQL.AsyncBukkitMySQL;
import net.mcstats2.core.api.config.Configuration;
import net.mcstats2.core.api.config.ConfigurationProvider;
import net.mcstats2.core.api.config.YamlConfiguration;
import net.mcstats2.core.exceptions.MCSError;
import net.mcstats2.core.exceptions.MCSServerAuthFailed;
import net.mcstats2.core.exceptions.MCSServerRegistrationFailed;
import net.mcstats2.core.network.web.data.MCSFilterData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class Core extends JavaPlugin {
    private MCSCore mcs;
    private AsyncBukkitMySQL mysql;

    private static Core instance;
    private CommandSender console;

    private Configuration config;

    public JsonObject players = new JsonObject();
    public ArrayList<MCSFilterData> badwords = new ArrayList<>();

    @Override
    public void onEnable() {
        File plugindir = getDataFolder();
        if(!plugindir.exists())
            plugindir.mkdir();
        try {
            File file = new File(getDataFolder(), "config.yml");
            if(!file.exists()) {
                file.createNewFile();

                InputStream is = getResource("config.yml");
                OutputStream os = new FileOutputStream(file);
                ByteStreams.copy(is, os);
            }

            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        instance = this;
        console = getConsole();

        mysql = new AsyncBukkitMySQL(this, config.getString("MySQL.hostname"), config.getInt("MySQL.port"), config.getString("MySQL.username"), config.getString("MySQL.password"), config.getString("MySQL.database"));
        if (mysql.getMySQL().isConnected())
            //console.sendMessage("§aDatabase connected!");
        {} else {
            //console.sendMessage("§aDatabase connection failure!");
            getServer().shutdown();
        }

        try {
            MCSCore mcs = new MCSCore(getDataFolder(), new MCSBukkitServer(this), mysql.getMySQL());
        } catch (MCSError | MCSServerRegistrationFailed | IOException | ExecutionException | InterruptedException | MCSServerAuthFailed e) {
            e.printStackTrace();
        }


        File cachedir = new File(getDataFolder().getPath() + "/cache/");
        if(!cachedir.exists())
            cachedir.mkdir();

        File chatdir = new File(cachedir.getPath() + "/chat/");
        if(!chatdir.exists())
            chatdir.mkdir();

        File filterdir = new File(chatdir.getPath() + "/filter/");
        if(!filterdir.exists())
            filterdir.mkdir();

        for (File file : Objects.requireNonNull(filterdir.listFiles())) {
            if (!file.getName().endsWith(".json"))
                continue;

            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                badwords.add(new Gson().fromJson(br, MCSFilterData.class));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (br != null)
                        br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        //getServer().getPluginManager().registerListener(this, new PlayerJoin(this));
        //getServer().getPluginManager().registerListener(this, new PlayerQuit(this));

        /*if (config.getBoolean("Modules.ChatFilter.enabled"))
            getServer().getPluginManager().registerListener(this, new ChatFilter(this));*/


        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            if (config.getBoolean("Modules.Kick.enabled"))
                commandMap.register("kick", new CommandManager("kick", null, null, new ArrayList<String>()));

            if (config.getBoolean("Modules.Mute.enabled")) {
                commandMap.register("cmute", new CommandManager("cmute", null, null, new ArrayList<String>()));
                commandMap.register("mute", new CommandManager("mute", null, null, new ArrayList<String>()));
                commandMap.register("unmute", new CommandManager("unmute", null, null, new ArrayList<String>()));
            }

            if (config.getBoolean("Modules.Ban.enabled")) {
                commandMap.register("cban", new CommandManager("cban", null, null, new ArrayList<String>()));
                commandMap.register("ban", new CommandManager("ban", null, null, new ArrayList<String>()));
                commandMap.register("unban", new CommandManager("unban", null, null, new ArrayList<String>()));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        mcs.end();
        mysql.getMySQL().closeConnection();
    }

    public static Core getInstance() {
        return instance;
    }

    public Configuration getConfigg() {
        return config;
    }

    public CommandSender getConsole() {
        return console;
    }
}
