package net.mcstats2.bridge.server.bukkit;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSServer.MCSBukkitServer;
import net.mcstats2.core.network.mysql.AsyncBukkitMySQL;
import net.mcstats2.core.api.config.Configuration;
import net.mcstats2.core.api.config.ConfigurationProvider;
import net.mcstats2.core.api.config.YamlConfiguration;
import net.mcstats2.core.exceptions.MCSError;
import net.mcstats2.core.exceptions.MCSServerAuthFailed;
import net.mcstats2.core.exceptions.MCSServerRegistrationFailed;
import net.mcstats2.core.network.web.data.MCSFilterData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class Core extends JavaPlugin {
    private MCSCore mcs;
    private AsyncBukkitMySQL mysql;

    private static Core instance;
    private CommandSender console;

    private Configuration config;

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
            mcs = new MCSCore(getFile(), getDataFolder(), config, new MCSBukkitServer(this), mysql.getMySQL());
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

        getServer().getScheduler().runTaskLater(this, () -> {
            for (String command : mcs.getCommands()) {
                if (command == null)
                    continue;

                if (!getDescription().getCommands().containsKey(command)) {
                    System.out.println("§cCommand " + command + " could not be registered!");
                    continue;
                }
                Bukkit.getPluginCommand(command).setExecutor(new CommandManager());
            }
        }, 20);
    }

    @Override
    public void onDisable() {
        mcs.shutdown();
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