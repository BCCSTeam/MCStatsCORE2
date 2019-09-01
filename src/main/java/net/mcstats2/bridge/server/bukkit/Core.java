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
import net.mcstats2.core.network.web.MCSData.MCSFilterData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
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

        for (File file : filterdir.listFiles()) {
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

        /*
        getProxy().getPluginManager().registerListener(this, new PlayerJoin(this));
        getProxy().getPluginManager().registerListener(this, new PlayerQuit(this));

        if (config.getBoolean("Modules.ChatFilter.enabled"))
            getProxy().getPluginManager().registerListener(this, new ChatFilter(this));

        if (config.getBoolean("Modules.Kick.enabled"))
            getProxy().getPluginManager().registerCommand(this, new Kick("kick"));

        if (config.getBoolean("Modules.Mute.enabled")) {
            getProxy().getPluginManager().registerCommand(this, new MuteCustom("cmute"));
            getProxy().getPluginManager().registerCommand(this, new Mute("mute"));
            getProxy().getPluginManager().registerCommand(this, new MuteRemove("unmute"));
        }

        if (config.getBoolean("Modules.Ban.enabled")) {
            getProxy().getPluginManager().registerCommand(this, new BanCustom("cban"));
            getProxy().getPluginManager().registerCommand(this, new Ban("ban"));
            getProxy().getPluginManager().registerCommand(this, new BanRemove("unban"));
        }*/
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
