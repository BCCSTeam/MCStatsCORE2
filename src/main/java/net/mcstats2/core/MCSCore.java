package net.mcstats2.core;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.gson.*;
import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.api.Command;
import net.mcstats2.core.api.MCSEntity.MCSEntity;
import net.mcstats2.core.api.MCSEvent.MCSEvent;
import net.mcstats2.core.api.MCSEvent.MCSEventType;
import net.mcstats2.core.api.MCSEvent.player.MCSEventPlayerUpdate;
import net.mcstats2.core.api.MCSServer.MCSBungeeServer;
import net.mcstats2.core.api.MCSServer.MCSServer;
import net.mcstats2.core.network.mysql.MySQL;
import net.mcstats2.core.api.commands.*;
import net.mcstats2.core.exceptions.MCSError;
import net.mcstats2.core.exceptions.MCSServerAuthFailed;
import net.mcstats2.core.exceptions.MCSServerRegistrationFailed;
import net.mcstats2.core.network.web.data.MCSAuthData;
import net.mcstats2.core.network.web.data.MCSPlayerData;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.mcstats2.core.network.web.data.MCSQueryData;
import net.mcstats2.core.network.web.RequestBuilder;
import net.mcstats2.core.network.web.RequestResponse;
import net.mcstats2.core.api.config.Configuration;
import net.mcstats2.core.api.config.ConfigurationProvider;
import net.mcstats2.core.api.config.YamlConfiguration;
import net.mcstats2.core.network.web.data.MCSUpdaterData;
import net.mcstats2.core.network.web.data.task.MCSTaskData;
import net.mcstats2.core.network.web.data.task.MCSTaskType;
import net.mcstats2.core.network.web.data.task.player.*;
import net.mcstats2.core.network.web.data.task.server.MCSTaskServerBanCreateReason;
import net.mcstats2.core.network.web.data.task.server.MCSTaskServerMuteCreateReason;
import net.mcstats2.core.utils.version.Version;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MCSCore {
    private static MCSCore instance;
    private MCSServer server;
    private MySQL mysql;

    private String API_SERVER = "https://api.mcstats.net/v2/server/";
    public Version API_PARSER_VERSION;
    public MCSUpdaterData UPDATE = null;

    private String GUID;
    private String Secret;
    private String serverID;
    private String instanceID;

    private HashMap<String, UUID> name2UUID = new HashMap<>();
    private HashMap<UUID, MCSPlayerData> players = new HashMap<>();

    private Timer t = new Timer();
    private long cooldown = 0;
    private HashMap<MCSAuthData.Response.URL, String> urls = new HashMap<>();

    private HashMap<MCSTaskData, Long> running = new HashMap<>();
    private HashMap<Integer, Long> cooldowns = new HashMap<>();

    private static final Pattern argsSplit = Pattern.compile(" ");
    private final Map<String, Command> commandMap = new HashMap<>();

    private final Map<MCSEventType, List<Consumer<MCSEvent>>> eventMap = new HashMap<>();

    private File pluginDir;
    private Configuration lang = null;
    private HashMap<String, Configuration> langs = new HashMap<>();

    public MCSCore(File pluginDir, MCSServer server, MySQL database) throws MCSError, MCSServerRegistrationFailed, IOException, ExecutionException, InterruptedException, MCSServerAuthFailed {
        instance = this;
        this.pluginDir = pluginDir;
        this.server = server;
        mysql = database;

        API_PARSER_VERSION = new Version(server.getDescription().getVersion());

        if(!pluginDir.exists())
            pluginDir.mkdir();

        File langdir = new File(pluginDir.getPath() + "/lang/");
        if(!langdir.exists())
            langdir.mkdir();

        try {
            if (lang == null) {
                File d = new File(pluginDir.getPath() + "/lang/", "default.yml");
                if (!d.exists()) {
                    d.createNewFile();

                    InputStream is = null;
                    try {
                        is = getClass().getClassLoader().getResourceAsStream("default-lang.yml");
                        OutputStream os = new FileOutputStream(d);
                        ByteStreams.copy(is, os);
                    } finally {
                        if (is != null)
                            is.close();
                    }
                }

                InputStream a = getClass().getClassLoader().getResourceAsStream("default-lang.yml");
                Configuration def = ConfigurationProvider.getProvider(YamlConfiguration.class).load(a);
                this.lang = ConfigurationProvider.getProvider(YamlConfiguration.class).load(d, def);
                a.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(pluginDir, "license.yml");
        if (!file.exists())
            file.createNewFile();

        Configuration license = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

        if (license.getString("GUID")==null || license.getString("GUID").isEmpty() || license.getString("Secret")==null || license.getString("Secret").isEmpty()) {
            RequestBuilder rb = new RequestBuilder(API_SERVER + "register");

            rb.putHeader("API-Parser-Version", API_PARSER_VERSION);

            rb.putParam("details[os][java]", System.getProperty("java.version"));
            rb.putParam("details[os][name]", System.getProperty("os.name"));
            rb.putParam("details[os][arch]", System.getProperty("os.arch"));
            rb.putParam("details[os][version]", System.getProperty("os.version"));
            rb.putParam("details[os][cores]", Runtime.getRuntime().availableProcessors());


            rb.putParam("details[server][is_cloud]", server.getServerDetails().isCloudSystem());
            if (server.getServerDetails().isCloudSystem()) {
                rb.putParam("details[server][cloud][id]", server.getServerDetails().getCloudSystem().getId());
                rb.putParam("details[server][cloud][wrapper]", server.getServerDetails().getCloudSystem().getWrapperId());
                rb.putParam("details[server][cloud][name]", server.getServerDetails().getCloudSystem().getGroup());
                rb.putParam("details[server][cloud][static]", server.getServerDetails().getCloudSystem().isStatic());
            }

            rb.putParam("details[server][port]", server.getServerDetails().getPort());
            rb.putParam("details[server][onlinemode]", server.getServerDetails().isOnlineMode());
            rb.putParam("details[server][type]", server.getServerDetails().getType().name());
            rb.putParam("details[server][version]", server.getServerDetails().getVersion());

            long freeMem = Runtime.getRuntime().freeMemory();
            long maxMem = Runtime.getRuntime().maxMemory();
            rb.putParam("details[memory][free]", freeMem);
            rb.putParam("details[memory][used]", maxMem - freeMem);
            rb.putParam("details[memory][total]", Runtime.getRuntime().totalMemory());
            rb.putParam("details[memory][max]", maxMem);

            rb.putParam("details[plugin][name]", server.getDescription().getName());
            rb.putParam("details[plugin][author]", server.getDescription().getAuthor());
            rb.putParam("details[plugin][version]", server.getDescription().getVersion());

            RequestResponse rr = rb.post();

            if (rr.getStatusCode() == 200) {
                JsonObject response = rr.getContentJsonObject().get("response").getAsJsonObject();
                JsonObject system = rr.getContentJsonObject().get("system").getAsJsonObject();

                if (system.get("status").getAsInt() == 200) {
                    GUID = response.get("GUID").getAsString();
                    Secret = response.get("Secret").getAsString();
                    serverID = response.get("serverID").getAsString();
                    instanceID = response.get("instanceID").getAsString();
                    //urls = response.get("urls").getAsJsonObject();

                    license.set("GUID", GUID);
                    license.set("Secret", Secret);
                    license.set("serverID", serverID);
                    license.set("instanceID", instanceID);
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(license, file);

                    server.sendConsole("");
                    server.sendConsole("§a----------{ SETUP PANEL START }----------");
                    server.sendConsole("§a1. Create a account at: " + urls.get(MCSAuthData.Response.URL.REGISTER));
                    server.sendConsole("§a2. Grant your server panel access at: " + urls.get(MCSAuthData.Response.URL.SERVER_ADD));
                    server.sendConsole("");
                    server.sendConsole("§aGUID: " + GUID);
                    server.sendConsole("§aSecret: " + Secret);
                    server.sendConsole("§a----------{ SETUP PANEL END   }----------");
                    server.sendConsole("");

                    start();
                } else
                    throw new MCSServerRegistrationFailed(system.get("message").getAsString());
            } else
                throw new MCSError(rr.getStatusLine().getReasonPhrase());
        } else {
            GUID = license.getString("GUID");
            Secret = license.getString("Secret");
            serverID = license.getString("serverID");
            instanceID = license.getString("instanceID");

            RequestBuilder rb = getAuthedRequest("auth");

            rb.putParam("details[os][java]", System.getProperty("java.version"));
            rb.putParam("details[os][name]", System.getProperty("os.name"));
            rb.putParam("details[os][arch]", System.getProperty("os.arch"));
            rb.putParam("details[os][version]", System.getProperty("os.version"));
            rb.putParam("details[os][cores]", Runtime.getRuntime().availableProcessors());


            rb.putParam("details[server][is_cloud]", server.getServerDetails().isCloudSystem());
            if (server.getServerDetails().isCloudSystem()) {
                rb.putParam("details[server][cloud][id]", server.getServerDetails().getCloudSystem().getId());
                rb.putParam("details[server][cloud][wrapper]", server.getServerDetails().getCloudSystem().getWrapperId());
                rb.putParam("details[server][cloud][name]", server.getServerDetails().getCloudSystem().getGroup());
                rb.putParam("details[server][cloud][static]", server.getServerDetails().getCloudSystem().isStatic());
            }

            rb.putParam("details[server][port]", server.getServerDetails().getPort());
            rb.putParam("details[server][onlinemode]", server.getServerDetails().isOnlineMode());
            rb.putParam("details[server][type]", server.getServerDetails().getType().name());
            rb.putParam("details[server][version]", server.getServerDetails().getVersion());

            long freeMem = Runtime.getRuntime().freeMemory();
            long maxMem = Runtime.getRuntime().maxMemory();
            rb.putParam("details[memory][free]", freeMem);
            rb.putParam("details[memory][used]", maxMem - freeMem);
            rb.putParam("details[memory][total]", Runtime.getRuntime().totalMemory());
            rb.putParam("details[memory][max]", maxMem);

            rb.putParam("details[plugin][name]", server.getDescription().getName());
            rb.putParam("details[plugin][author]", server.getDescription().getAuthor());
            rb.putParam("details[plugin][version]", server.getDescription().getVersion());

            RequestResponse rr = rb.post();

            if (rr.getStatusCode() == 200) {
                JsonObject response = rr.getContentJsonObject().get("response").getAsJsonObject();
                JsonObject system = rr.getContentJsonObject().get("system").getAsJsonObject();

                if (system.get("status").getAsInt() == 200) {
                    if (!serverID.equals(response.get("serverID").getAsString())) {
                        serverID = response.get("serverID").getAsString();

                        license.set("serverID", serverID);
                        ConfigurationProvider.getProvider(YamlConfiguration.class).save(license, file);

                        server.sendConsole("§aUpdated §2serverID");
                    }

                    if (!instanceID.equals(response.get("instanceID").getAsString())) {
                        instanceID = response.get("instanceID").getAsString();

                        license.set("instanceID", instanceID);
                        ConfigurationProvider.getProvider(YamlConfiguration.class).save(license, file);

                        server.sendConsole("§aUpdated §2instanceID");
                    }
                    //serverID = response.get("serverID").getAsString();
                    //authed = true;
                    //JsonObject urls = response.get("urls").getAsJsonObject();

                    /*if (!response.get("hasAdminPanel").getAsBoolean()) {
                        server.sendConsole("");
                        server.sendConsole("§a----------{ SETUP PANEL START }----------");
                        server.sendConsole("§a1. Create a account at: " + urls.get("register").getAsString());
                        server.sendConsole("§a2. Grant your server panel access at: " + urls.get("server-add").getAsString());
                        server.sendConsole("");
                        server.sendConsole("§aGUID: " + GUID);
                        server.sendConsole("§aSecret: " + Secret);
                        server.sendConsole("§a----------{ SETUP PANEL END   }----------");
                        server.sendConsole("");
                    }*/

                    start();
                } else
                    throw new MCSServerAuthFailed(system.get("message").getAsString());
            } else
                throw new MCSError(rr.getStatusLine().getReasonPhrase());
        }
    }

    public static MCSCore getInstance() {
        return instance;
    }

    public MySQL getMySQL() {
        return mysql;
    }

    public MCSServer getServer() {
        return server;
    }

    private void start() {
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    RequestBuilder rb = getAuthedRequest("query");

                    StringBuilder players = new StringBuilder();
                    for (MCSPlayer p : server.getPlayers()) {
                        if (players.length() > 0)
                            players.append(",");
                        players.append(p.getUUID().toString());
                    }
                    rb.putParam("players", players.toString());

                    long freeMem = Runtime.getRuntime().freeMemory();
                    long maxMem = Runtime.getRuntime().maxMemory();
                    rb.putParam("details[memory][free]", freeMem);
                    rb.putParam("details[memory][used]", maxMem - freeMem);

                    MCSQueryData data = pharseQuery(rb.post());

                    if (data == null)
                        return;

                    if (!serverID.equals(data.response.serverID)) {
                        serverID = data.response.serverID;

                        File file = new File(pluginDir, "license.yml");
                        Configuration license = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
                        license.set("serverID", serverID);
                        ConfigurationProvider.getProvider(YamlConfiguration.class).save(license, file);

                        server.sendConsole("§aUpdated §2serverID");
                    }

                    urls = data.response.URLs;

                    if (!data.response.hasAdminPanel && (cooldown + 1000*60*5) <= System.currentTimeMillis()) {
                        cooldown = System.currentTimeMillis();
                        server.sendConsole("");
                        server.sendConsole("§a----------{ SETUP PANEL START }----------");
                        server.sendConsole("§a1. Create a account at: " + urls.get(MCSAuthData.Response.URL.REGISTER));
                        server.sendConsole("§a2. Grant your server panel access at: " + urls.get(MCSAuthData.Response.URL.SERVER_ADD));
                        server.sendConsole("");
                        server.sendConsole("§aGUID: " + GUID);
                        server.sendConsole("§aSecret: " + Secret);
                        server.sendConsole("§a----------{ SETUP PANEL END   }----------");
                        server.sendConsole("");
                    }

                    ArrayList<String> failed = new ArrayList<>();
                    running.forEach((a, b) -> {
                        if (b <= System.currentTimeMillis() + 1000*60*5) {
                            try {
                                failed.add(a.getId());
                                updateTaskState(a, TaskState.TIMEOUT);
                            } catch (InterruptedException | ExecutionException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    for (MCSTaskData task : data.response.tasks) {
                        try {
                            if (failed.contains(task.getId()))
                                continue;

                            if (running.containsKey(task))
                                continue;

                            running.put(task, System.currentTimeMillis());

                            if (task.getType().equals(MCSTaskType.PLAYER_UPDATE)) {
                                MCSTaskPlayerUpdate j = (MCSTaskPlayerUpdate) task.getTask();

                                getPlayer(j.getUUID(), true);

                                updateTaskState(task, TaskState.DONE);

                            } else if (task.getType().equals(MCSTaskType.PLAYER_MESSAGE)) {
                                MCSTaskPlayerMessage j = (MCSTaskPlayerMessage) task.getTask();

                                MCSPlayer p = getPlayer(j.getReceiver());
                                if (p.isOnline())
                                    p.sendMessage(j.getMessage());

                                updateTaskState(task, TaskState.DONE);

                            } else if (task.getType().equals(MCSTaskType.PLAYER_ALERT)) {
                                MCSTaskPlayerAlert j = (MCSTaskPlayerAlert) task.getTask();

                                if (j.getPermission() != null)
                                    getServer().broadcast(j.getPermission(), j.getMessage());
                                else
                                    getServer().broadcast(j.getMessage());

                                updateTaskState(task, TaskState.DONE);

                            } else if (task.getType().equals(MCSTaskType.PLAYER_KICK)) {
                                MCSTaskPlayerKick j = (MCSTaskPlayerKick) task.getTask();

                                MCSPlayer p = getPlayer(j.getUUID());

                                HashMap<String, Object> replace = new HashMap<>();
                                replace.put("reason", j.getReason());
                                p.disconnect(MCSCore.getInstance().buildScreen(p.getLang(), "kick.screen", replace));

                                replace.put("playername", p.getName());
                                replace.put("staffname", p.getName());
                                try {
                                    MCSCore.getInstance().broadcast("MCStatsNET.kick.alert", "kick.prefix", "kick.alert", replace);
                                } catch (InterruptedException | ExecutionException | IOException e) {
                                    e.printStackTrace();
                                }

                                updateTaskState(task, TaskState.DONE);

                            } else if (task.getType().equals(MCSTaskType.PLAYER_MUTE)) {
                                MCSTaskPlayerMute j = (MCSTaskPlayerMute) task.getTask();

                                MCSPlayer p = getPlayer(j.getUUID());
                                MCSPlayer staff = getPlayer(j.getSTAFF());

                                if (j.getTemplate() != null)
                                    p.createMute(staff, j.getTemplate());

                                updateTaskState(task, TaskState.DONE);

                            } else if (task.getType().equals(MCSTaskType.PLAYER_BAN)) {
                                MCSTaskPlayerBan j = (MCSTaskPlayerBan) task.getTask();

                                MCSPlayer p = getPlayer(j.getUUID());
                                MCSPlayer staff = getPlayer(j.getSTAFF());

                                if (j.getTemplate() != null)
                                    p.createBan(staff, j.getTemplate());

                                updateTaskState(task, TaskState.DONE);

                            } else if (task.getType().equals(MCSTaskType.PLAYER_CMUTE)) {
                                MCSTaskPlayerCMute j = (MCSTaskPlayerCMute) task.getTask();

                                MCSPlayer p = getPlayer(j.getUUID());
                                MCSPlayer staff = getPlayer(j.getSTAFF());

                                p.createCustomMute(staff, j.getText(), j.getExpire());

                                updateTaskState(task, TaskState.DONE);

                            } else if (task.getType().equals(MCSTaskType.PLAYER_CBAN)) {
                                MCSTaskPlayerCBan j = (MCSTaskPlayerCBan) task.getTask();

                                MCSPlayer p = getPlayer(j.getUUID());
                                MCSPlayer staff = getPlayer(j.getSTAFF());

                                p.createCustomBan(staff, j.getText(), j.getExpire());

                                updateTaskState(task, TaskState.DONE);

                            } else if (task.getType().equals(MCSTaskType.PLAYER_GMUTE) || task.getType().equals(MCSTaskType.PLAYER_GBAN)) {
                                MCSTaskPlayerKick j = (MCSTaskPlayerKick) task.getTask();

                                MCSPlayer p = getPlayer(j.getUUID());
                                p.disconnect(j.getReason());

                                updateTaskState(task, TaskState.DONE);

                            } else if (task.getType().equals(MCSTaskType.SERVER_MUTE_CREATE_REASON)) {
                                MCSTaskServerMuteCreateReason j = (MCSTaskServerMuteCreateReason) task.getTask();

                                createMuteTemplate(j.getName(), j.getReason(), j.getPower(), j.getExpires());

                                updateTaskState(task, TaskState.DONE);

                            } else if (task.getType().equals(MCSTaskType.SERVER_BAN_CREATE_REASON)) {
                                MCSTaskServerBanCreateReason j = (MCSTaskServerBanCreateReason) task.getTask();

                                createBanTemplate(j.getName(), j.getReason(), j.getPower(), j.getExpires());

                                updateTaskState(task, TaskState.DONE);

                                /*
                            } else if (task.getType().equals(MCSTaskType.SERVER_BAN_CREATE_REASON)) {
                                MCSTaskServerBanCreateReason j = (MCSTaskServerBanCreateReason) task.getTask();

                                updateTaskState(task, TaskState.DONE);
                                 */
                            } else
                                updateTaskState(task, TaskState.UNSUPPORTED);
                        } catch (Exception e) {
                            updateTaskState(task, TaskState.FAILED);
                            e.printStackTrace();
                        }
                    }

                    for (MCSQueryData.Response.Alert alert : data.response.alerts) {
                        if (cooldowns.containsKey(alert.text.hashCode()))
                            if (alert.delay == 0 || cooldowns.get(alert.text.hashCode()) + 1000*alert.delay >= System.currentTimeMillis())
                                continue;

                        cooldowns.put(alert.text.hashCode(), System.currentTimeMillis());
                        server.sendConsole(alert.type.getChatColor() + alert.type.getPrefix() + " " + alert.text);
                    }
                } catch (IOException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        },0,1000);

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkUpdate(true);
            }
        }, 0, 1000 * 60 * 5);

        registerCommands();
    }

    public Timer getTimer() {
        return t;
    }

    public void end() {
        if (t != null) {
            t.purge();
            t.cancel();
            t = null;
        }
    }

    public void reload() {
        try {
            langs.keySet().forEach(a -> langs.put(a, getLang(a)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkUpdate(boolean autoupdate) {
        try {
            server.sendConsole("§7Checking for Update...");

            RequestBuilder rb = getAuthedRequest("updater");
            RequestResponse rr = rb.post();
            if (rr.getStatusCode() != 200)
                throw new MCSError(rr.getStatusLine().getReasonPhrase());

            MCSUpdaterData data = new Gson().fromJson(rr.getContent(), MCSUpdaterData.class);

            if (data.getSystem().getStatus() != 200) {
                UPDATE = null;
                throw new MCSError(data.getSystem().getMessage());
            }

            UPDATE = data;

            if (!API_PARSER_VERSION.isEqual(data.getResponse().getVersion())) {
                if (API_PARSER_VERSION.isHigherThan(data.getResponse().getVersion()))
                    throw new MCSError("Version error! Please check manual for Updates, in cause if this error stay please contact support at support@mcstats.net and wait for future instructions!");

                runUpdate(data, true, true);
            } else
                server.sendConsole("§aRunning newest version(" + data.getResponse().getVersion() + ")!");
        } catch (IOException | InterruptedException | ExecutionException | MCSError e) {
            e.printStackTrace();
        }
    }

    public void runUpdate(MCSUpdaterData data, boolean autoreplace, boolean autorestart) throws MCSError, IOException, ExecutionException, InterruptedException {
        server.sendConsole("§6An new version(" + data.getResponse().getVersion() + ") is available for download! Downloading...");
        if (!data.getResponse().getDownloadURL().startsWith("https://mcstats.net/") && !data.getResponse().getDownloadURL().startsWith("https://www.mcstats.net/") && !data.getResponse().getDownloadURL().startsWith("https://api.mcstats.net/"))
            throw new MCSError("Unsafe download URL! Please check manual for Updates, in cause if this error stay please contact support at support@mcstats.net and wait for future instructions!");

        File home = server instanceof MCSBungeeServer ? server.getDescription().getPlugin() : new File(server.getDescription().getPlugin(), "MCStatsCORE2-" + server.getDescription().getVersion() + ".jar");
        File a = new File(pluginDir.getPath() + "/cache/", "MCStatsCORE2-" + data.getResponse().getVersion() + ".jar");
        RequestBuilder rb = new RequestBuilder(data.getResponse().getDownloadURL());
        rb.download(a);

        if (server.getServerDetails().isCloudSystem()) {
            server.sendConsole("§eUpdate downloaded! Can't moving file(MCStatsCORE2-" + data.getResponse().getVersion() + ".jar)...");
            return;
        }

        if (autoreplace) {
            server.sendConsole("§eUpdate downloaded! Moving file(MCStatsCORE2-" + data.getResponse().getVersion() + ".jar)...");

            Files.move(a, home);
        }

        if (autorestart) {
            server.sendConsole("§aUpdate moved! Stopping server...");

            server.shutdown();
        }
    }

    private void updateTaskState(MCSTaskData task, TaskState state) throws InterruptedException, ExecutionException, IOException {
        RequestBuilder rb = getAuthedRequest("task/" + task.getId() + "/update");
        rb.putParam("status", state.getCode());
        rb.post();

        running.remove(task);
    }

    private MCSQueryData pharseQuery(RequestResponse rs) throws IOException {
        if (rs.getStatusCode() != 200)
            return null;

        MCSQueryData data = new Gson().fromJson(rs.getContent(), MCSQueryData.class);

        if (data.system.status != 200)
            return null;

        return data;
    }

    public RequestBuilder getAuthedRequest(String path) {
        RequestBuilder rb = new RequestBuilder(API_SERVER + GUID + (path.startsWith("/") ? "" : "/") + path);
        rb.putHeader("Auth-Instance", instanceID);
        rb.putHeader("Auth-Secret", Secret);
        rb.putHeader("API-Parser-Version", API_PARSER_VERSION.getOriginalString());

        return rb;
    }

    public MCSPlayer playerJoin(UUID uuid, String playername, String address, String connect, int version) throws IOException, InterruptedException, ExecutionException {
        RequestBuilder rb = getAuthedRequest("/player/" + uuid.toString() + "/join");

        rb.putParam("playername", playername);
        rb.putParam("address", address);
        rb.putParam("domain", connect);
        rb.putParam("version", version);

        return getPlayer(rb.post());
    }

    public void playerQuit(UUID uuid) throws IOException, InterruptedException, ExecutionException {
        RequestBuilder rb = getAuthedRequest("/player/" + uuid.toString() + "/quit");

        rb.post();

        players.remove(uuid);
    }

    public MCSPlayer getPlayer(UUID uuid) throws IOException, InterruptedException, ExecutionException {
        return getPlayer(uuid, false);
    }

    public MCSPlayer getPlayer(UUID uuid, boolean force) throws IOException, InterruptedException, ExecutionException {
        if (players.containsKey(uuid) && !force)
            return new MCSPlayer(players.get(uuid));

        MCSPlayer player = getPlayer(uuid.toString());

        if (force)
            executeEvent(MCSEventType.PLAYER_UPDATE, player);

        return player;
    }
    public MCSPlayer getPlayer(String player) throws IOException, InterruptedException, ExecutionException {
        if (name2UUID.containsKey(player) && players.containsKey(name2UUID.get(player)))
            return new MCSPlayer(players.get(name2UUID.get(player)));

        RequestBuilder rb = getAuthedRequest("/player/" + player + "/details");

        return getPlayer(rb.post());
    }
    private MCSPlayer getPlayer(RequestResponse rs) throws IOException {
        if (rs.getStatusCode() != 200)
            return null;

        MCSPlayerData data = new Gson().fromJson(rs.getContent(), MCSPlayerData.class);

        if (data.system.status != 200)
            return null;

        name2UUID.put(data.response.name, UUID.fromString(data.response.UUID));
        players.put(UUID.fromString(data.response.UUID), data);

        return new MCSPlayer(data);
    }

    public void registerEvent(MCSEventType type, Consumer<MCSEvent> consumer) {
        List<Consumer<MCSEvent>> events = eventMap.getOrDefault(type, new ArrayList<>());
        events.add(consumer);
        eventMap.put(type, events);
    }
    public void executeEvent(MCSEventType type, Object... data) {
        if (!eventMap.containsKey(type))
            return;

        eventMap.get(type).forEach(a -> a.accept(new MCSEventPlayerUpdate((MCSPlayer) data[0])));
    }

    private void registerCommands() {
        registerCommand(new MCStats("mcstats"));

        if (getServer().getClass().getName().startsWith("MCSBungeeServer"))
            registerCommand(new Jump("jump"));

        registerCommand(new Kick("kick"));

        registerCommand(new MuteCustom("cmute"));
        registerCommand(new Mute("mute"));
        registerCommand(new MuteRemove("unmute"));

        registerCommand(new BanCustom("cban"));
        registerCommand(new Ban("ban"));
        registerCommand(new BanRemove("unban"));
    }

    private void registerCommand(Command command) {
        commandMap.put(command.getName().toLowerCase(), command);

        if (command.getAliases() != null)
            Arrays.stream(command.getAliases()).forEach(alias -> this.commandMap.put(alias.toLowerCase(), command));
    }

    private void unregisterCommand(Command command) {
        while(commandMap.values().remove(command)) {
        }
    }

    public boolean dispatchCommand(MCSEntity sender, String commandLine) {
        String[] split = argsSplit.split(commandLine, -1);
        if (split.length == 0) {
            return false;
        } else {
            String name = split[0].toLowerCase();
            String[] args = Arrays.copyOfRange(split, 1, split.length);

            return dispatchCommand(sender, name, args);
        }
    }

    public boolean dispatchCommand(MCSEntity sender, String name, String[] args) {
        Command command = commandMap.get(name);
        if (command == null) {
            return false;
        } else {
            String permission = command.getPermission();
            if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
                sender.sendMessage("no_permission");

                return true;
            } else {
                try {
                    command.execute(sender, args);
                } catch (Exception var11) {
                    sender.sendMessage(ChatColor.RED + "An internal error occurred whilst executing this command, please check the console log for details.");
                    var11.printStackTrace();
                }

                return true;
            }
        }
    }


    public MuteTemplate createMuteTemplate(String name, String reason, int power, List<Integer> expires_) throws SQLException {
        String id = randomString(10);
        String expires = new Gson().toJson(expires_);

        if (getMySQL().queryUpdate("INSERT INTO `MCSCore__mutes-templates`(`id`, `name`, `text`, `power`, `expires`) VALUES (?,?,?,?,?)", id, name, reason, power, expires) != 0)
            return getMuteTemplateByID(id);
        else
            return null;
    }

    public MuteTemplate getMuteTemplateByID(String id) throws SQLException {
        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__mutes-templates` WHERE `id`=? LIMIT 1", id);
        if (!rs.next())
            return null;
        return new MuteTemplate(rs);
    }
    public MuteTemplate getMuteTemplateByName(String name) throws SQLException {
        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__mutes-templates` WHERE `name`=? LIMIT 1", name);
        if (!rs.next())
            return null;
        return new MuteTemplate(rs);
    }
    public MuteTemplate[] getMuteTemplatesByName(String name) throws SQLException {
        List<MuteTemplate> templates = new ArrayList<>();

        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__mutes-templates` WHERE `name` LIKE ?",name + "%");
        while (rs.next())
            templates.add(new MuteTemplate(rs));

        MuteTemplate[] tp = new MuteTemplate[templates.size()];
        templates.toArray(tp);
        return tp;
    }

    public MuteTemplate[] getMuteTemplates(int power) throws SQLException {
        return getMuteTemplates("", power);
    }
    public MuteTemplate[] getMuteTemplates(String name, int power) throws SQLException {
        List<MuteTemplate> templates = new ArrayList<>();

        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__mutes-templates` WHERE `name` LIKE ? && `power`<=?", name + "%", power);
        while (rs.next())
            templates.add(new MuteTemplate(rs));

        MuteTemplate[] tp = new MuteTemplate[templates.size()];
        templates.toArray(tp);
        return tp;
    }
    public MuteTemplate getMuteTemplateByPower(int minPower) throws SQLException {
        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__mutes-templates` WHERE `power`<=? LIMIT 1", minPower);
        if (!rs.next())
            return null;
        return new MuteTemplate(rs);
    }
    public class MuteTemplate {
        private String id;
        private String name;
        private String text;
        private int power;
        private String expires;

        MuteTemplate(ResultSet rs) throws SQLException {
            id = rs.getString("id");
            name = rs.getString("name");
            text = rs.getString("text");
            power = rs.getInt("power");
            expires = rs.getString("expires");
        }

        public String getID() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getText() {
            return text;
        }

        public int getPower() {
            return power;
        }

        public List<Integer> getExpires() {
            List<Integer> expires = new ArrayList<>();

            JsonParser parser = new JsonParser();
            JsonElement json = parser.parse(this.expires);
            for (JsonElement je : json.getAsJsonArray())
                expires.add(je.getAsInt());

            return expires;
        }
    }


    public BanTemplate createBanTemplate(String name, String reason, int power, List<Integer> expires_) throws SQLException {
        String id = randomString(10);
        String expires = new Gson().toJson(expires_);

        if (getMySQL().queryUpdate("INSERT INTO `MCSCore__bans-templates`(`id`, `name`, `text`, `power`, `expires`) VALUES (?,?,?,?,?)", id, name, reason, power, expires) != 0)
            return getBanTemplateByID(id);
        else
            return null;
    }

    public BanTemplate getBanTemplateByID(String id) throws SQLException {
        ResultSet rs = mysql.query("SELECT * FROM getMCSCore__bans-templates` WHERE `id`=? LIMIT 1", id);
        if (!rs.next())
            return null;
        return new BanTemplate(rs);
    }
    public BanTemplate getBanTemplateByName(String name) throws SQLException {
        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__bans-templates` WHERE `name`=? LIMIT 1", name);
        if (!rs.next())
            return null;
        return new BanTemplate(rs);
    }
    public BanTemplate[] getBanTemplatesByName(String name) throws SQLException {
        List<BanTemplate> templates = new ArrayList<>();

        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__bans-templates` WHERE `name` LIKE ?", name + "%");
        while (rs.next())
            templates.add(new BanTemplate(rs));

        BanTemplate[] tp = new BanTemplate[templates.size()];
        templates.toArray(tp);
        return tp;
    }
    public BanTemplate[] getBanTemplates(int power) throws SQLException {
        return getBanTemplates("", power);
    }
    public BanTemplate[] getBanTemplates(String name, int power) throws SQLException {
        List<BanTemplate> templates = new ArrayList<>();

        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__bans-templates` WHERE `name` LIKE ? && `power`<=?", name + "%", power);
        while (rs.next())
            templates.add(new BanTemplate(rs));

        BanTemplate[] tp = new BanTemplate[templates.size()];
        templates.toArray(tp);
        return tp;
    }
    public BanTemplate getBanTemplateByPower(int minPower) throws SQLException {
        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__bans-templates` WHERE `power`<=? LIMIT 1", minPower);
        if (!rs.next())
            return null;
        return new BanTemplate(rs);
    }
    public class BanTemplate {
        private String id;
        private String name;
        private String text;
        private int power;
        private String expires;

        BanTemplate(ResultSet rs) throws SQLException {
            id = rs.getString("id");
            name = rs.getString("name");
            text = rs.getString("text");
            power = rs.getInt("power");
            expires = rs.getString("expires");
        }

        public String getID() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getText() {
            return text;
        }

        public int getPower() {
            return power;
        }

        public List<Integer> getExpires() {
            List<Integer> expires = new ArrayList<>();

            JsonParser parser = new JsonParser();
            JsonElement json = parser.parse(this.expires);
            for (JsonElement je : json.getAsJsonArray())
                expires.add(je.getAsInt());

            return expires;
        }
    }

    private static final Random random = new Random();
    private static final String CHARS = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ234567890";
    public static String randomString(int length) {
        StringBuilder token = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            token.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return token.toString();
    }

    private enum TaskState {
        DONE(1),
        FAILED(-1),
        UNSUPPORTED(-2),
        TIMEOUT(-3);

        int code;

        TaskState(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }


    public Configuration getLang(String lang) {
        if (langs.containsKey(lang))
            return langs.get(lang);

        try {
            if (lang.equalsIgnoreCase("default"))
                return this.lang;

            File file = new File(pluginDir.getPath() + "/lang/", lang + ".yml");
            if (!file.exists()) {
                file.createNewFile();

                ConfigurationProvider.getProvider(YamlConfiguration.class).save(this.lang, file);
            }

            Configuration langc = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file, this.lang);
            langs.put(lang, langc);
            return langc;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void broadcast(String permission, String prefixPath, String path, HashMap<String, Object> replace) throws InterruptedException, ExecutionException, IOException {
        String message = lang.getString(path);

        for (Map.Entry r : replace.entrySet()) {
            if (r.getKey() != null)
                message = message.replace("%" + r.getKey().toString() + "%", r.getValue() != null ? r.getValue().toString() : "?");
        }

        getServer().sendConsole(ChatColor.translateAlternateColorCodes('&', lang.getString(prefixPath) + message));

        for (MCSPlayer p : getServer().getPlayers()) {
            if (p.hasPermission(permission)) {
                Configuration lang = getLang(p.getSession().getAddressDetails().getLanguage());

                message = lang.getString(path);

                for (Map.Entry r : replace.entrySet())
                    if (r.getKey() != null)
                        message = message.replace("%" + r.getKey().toString() + "%", r.getValue() != null ? r.getValue().toString() : "?");

                p.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString(prefixPath) + message));
            }
        }
    }

    public String buildScreen(Configuration lang, String path, HashMap<String, Object> replace) {
        String reason = "";

        for (String message : lang.getStringList(path)) {
            if (!reason.isEmpty())
                reason += "\n";

            for (Map.Entry r : replace.entrySet())
                if (r.getKey() != null)
                    message = message.replace("%" + r.getKey().toString() + "%", r.getValue() != null ? r.getValue().toString() : "?");

            reason += ChatColor.translateAlternateColorCodes('&', message);
        }

        return reason;
    }

    public static int getExpire(String timestr) {
        if (timestr.equals(0))
            return 0;

        Pattern tsr = Pattern.compile("([0-9]+[yMwdhms])");
        Matcher m = tsr.matcher(timestr);

        int time = 0;
        int i = 0;
        while (m.find()) {
            i++;
            String group = m.group(0);
            String key = group.substring(group.length() - 1);
            int value = Integer.parseInt(group.substring(0, group.length() - 1));

            switch (key) {
                case "y":
                    time += value * 12 * 4 * 7 * 24 * 60 * 60;
                    break;
                case "M":
                    time += value * 4 * 7 * 24 * 60 * 60;
                    break;
                case "w":
                    time += value * 7 * 24 * 60 * 60;
                    break;
                case "d":
                    time += value * 24 * 60 * 60;
                    break;
                case "h":
                    time += value * 60 * 60;
                    break;
                case "m":
                    time += value * 60;
                    break;
                case "s":
                    time += value;
                    break;
                default:
                    break;
            }
        }
        return i == 0 ? -1 : time;
    }
}
