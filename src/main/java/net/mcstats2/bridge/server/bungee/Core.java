package net.mcstats2.bridge.server.bungee;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.mcstats2.bridge.server.bungee.listeners.PlayerJoin;
import net.mcstats2.bridge.server.bungee.listeners.PlayerQuit;
import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.api.MCSServer.MCSBungeeServer;
import net.mcstats2.core.api.config.Configuration;
import net.mcstats2.core.api.config.ConfigurationProvider;
import net.mcstats2.core.api.config.YamlConfiguration;
import net.mcstats2.core.network.mysql.AsyncBungeeMySQL;
import net.mcstats2.core.network.web.data.MCSFilterData;
import net.mcstats2.core.exceptions.MCSError;
import net.mcstats2.core.exceptions.MCSServerAuthFailed;
import net.mcstats2.core.exceptions.MCSServerRegistrationFailed;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Core extends Plugin {
    private MCSCore mcs;
    private AsyncBungeeMySQL mysql;

    private static Core instance;
    private CommandSender console;

    private Configuration config;

    public JsonObject players = new JsonObject();
    public ArrayList<MCSFilterData> badwords = new ArrayList<>();

    Configuration news = null;
    int pointer = 0;

    @Override
    public void onEnable() {
        File plugindir = getDataFolder();
        if(!plugindir.exists())
            plugindir.mkdir();
        try {
            File file = new File(getDataFolder(), "config.yml");
            if(!file.exists()) {
                file.createNewFile();

                InputStream is = getResourceAsStream("config.yml");
                OutputStream os = new FileOutputStream(file);
                ByteStreams.copy(is, os);
            }

            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        instance = this;
        console = getProxy().getConsole();

        mysql = new AsyncBungeeMySQL(this, config.getString("MySQL.hostname"), config.getInt("MySQL.port"), config.getString("MySQL.username"), config.getString("MySQL.password"), config.getString("MySQL.database"));
        if (mysql.getMySQL().isConnected())
            console.sendMessage(TextComponent.fromLegacyText("§aDatabase connected!"));
        else {
            console.sendMessage(TextComponent.fromLegacyText("§aDatabase connection failure!"));
            getProxy().stop("Database Failure");
        }

        try {
            mcs = new MCSCore(getFile(), getDataFolder(), config, new MCSBungeeServer(this), mysql.getMySQL());
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

        getProxy().getPluginManager().registerListener(this, new PlayerJoin(this));
        getProxy().getPluginManager().registerListener(this, new PlayerQuit(this));

        getProxy().getScheduler().schedule(this, () -> {
            for (String command : mcs.getCommands())
                getProxy().getPluginManager().registerCommand(this, new CommandManager(command));
        }, 1, TimeUnit.SECONDS);


        loadNews();
        getProxy().getScheduler().schedule(this, () -> {
            if (!news.getBoolean("enabled"))
                return;

            if (!news.isSet("news.0"))
                return;

            if (getProxy().getOnlineCount() == 0)
                return;

            if (!news.isSet("news." + pointer))
                pointer = 0;

            StringBuilder sb = new StringBuilder();
            news.getStringList("news." + pointer).forEach(s -> {
                if (sb.length() != 0)
                    sb.append("\n");

                sb.append(ChatColor.translateAlternateColorCodes('&', s));
            });
            MCSCore.getInstance().getServer().broadcast(sb.toString());

            pointer++;
        }, 5, news.getInt("delay", 5), TimeUnit.SECONDS);
        MCSCore.getInstance().addToReload(() -> {
            loadNews();
            return null;
        });
    }

    private void loadNews() {
        try {
            File file = new File(getDataFolder(), "news.yml");
            if(!file.exists()) {
                file.createNewFile();

                InputStream is = getResourceAsStream("news.yml");
                OutputStream os = new FileOutputStream(file);
                ByteStreams.copy(is, os);
            }

            news = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        mcs.shutdown();
        mysql.getMySQL().closeConnection();


    }

    public static Core getInstance() {
        return instance;
    }

    public Configuration getConfig() {
        return config;
    }

    public CommandSender getConsole() {
        return console;
    }
}
