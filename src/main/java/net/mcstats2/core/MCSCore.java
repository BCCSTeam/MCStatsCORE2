package net.mcstats2.core;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.api.MCSEntity.MCSEntity;
import net.mcstats2.core.api.MCSEntity.MCSSystem;
import net.mcstats2.core.api.MCSServer.MCSServer;
import net.mcstats2.core.api.MySQL.MySQL;
import net.mcstats2.core.exceptions.MCSError;
import net.mcstats2.core.exceptions.MCSServerAuthFailed;
import net.mcstats2.core.exceptions.MCSServerRegistrationFailed;
import net.mcstats2.core.network.web.MCSData.MCSPlayerData;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.mcstats2.core.network.web.MCSData.MCSQueryData;
import net.mcstats2.core.network.web.RequestBuilder;
import net.mcstats2.core.network.web.RequestResponse;
import net.mcstats2.core.api.config.Configuration;
import net.mcstats2.core.api.config.ConfigurationProvider;
import net.mcstats2.core.api.config.YamlConfiguration;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MCSCore {
    private static MCSCore instance;
    private MCSServer server;
    private MySQL mysql;

    private String API_SERVER = "https://api.mcstats.net/v2/server/";

    private String GUID;
    private String Secret;
    private String serverID;
    private String instanceID;

    private HashMap<String, UUID> name2UUID = new HashMap<>();
    private HashMap<UUID, MCSPlayerData> players = new HashMap<>();

    private Timer t = new Timer();
    private long cooldown = 0;
    private HashMap<URLType, String> urls = new HashMap<>();

    private ArrayList<String> running = new ArrayList<>();
    private HashMap<Integer, Long> cooldowns = new HashMap<>();

    private File pluginDir;
    private Configuration lang = null;
    private HashMap<String, Configuration> langs = new HashMap<>();

    public MCSCore(File pluginDir, MCSServer server, MySQL database) throws MCSError, MCSServerRegistrationFailed, IOException, ExecutionException, InterruptedException, MCSServerAuthFailed {
        instance = this;
        this.pluginDir = pluginDir;
        this.server = server;
        mysql = database;

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

                    try (InputStream is = getClass().getClassLoader().getResourceAsStream("default-lang.yml");
                         OutputStream os = new FileOutputStream(d)) {
                        ByteStreams.copy(is, os);
                    }
                }

                Configuration def = ConfigurationProvider.getProvider(YamlConfiguration.class).load(getClass().getClassLoader().getResourceAsStream("default-lang.yml"));
                this.lang = ConfigurationProvider.getProvider(YamlConfiguration.class).load(d, def);
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

            rb.putParam("server-port", server.getServerDetails().getPort());
            rb.putParam("server-onlinemode", server.getServerDetails().isOnlineMode());
            rb.putParam("server-type", server.getServerDetails().getType().name());
            rb.putParam("server-version", server.getServerDetails().getVersion());
            rb.putParam("plugin-name", server.getDescription().getName());
            rb.putParam("plugin-author", server.getDescription().getAuthor());
            rb.putParam("plugin-version", server.getDescription().getVersion());

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
                    server.sendConsole("§a1. Create a account at: " + getUrl(URLType.REGISTER));
                    server.sendConsole("§a2. Grant your server panel access at: " + getUrl(URLType.SERVER_ADD));
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

            rb.putParam("server-port", server.getServerDetails().getPort());
            rb.putParam("server-onlinemode", server.getServerDetails().isOnlineMode());
            rb.putParam("server-type", server.getServerDetails().getType().name());
            rb.putParam("server-version", server.getServerDetails().getVersion());
            rb.putParam("plugin-name", server.getDescription().getName());
            rb.putParam("plugin-author", server.getDescription().getAuthor());
            rb.putParam("plugin-version", server.getDescription().getVersion());

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

                    String players = "";
                    for (MCSPlayer p : server.getPlayers()) {
                        if (!players.isEmpty())
                            players += ",";
                        players += p.getUUID().toString();
                    }
                    rb.putParam("players", players);

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

                    for (MCSQueryData.Response.URL url : data.response.URLs) {
                        urls.put(url.type, url.url);
                    }

                    if (!data.response.hasAdminPanel && (cooldown + 1000*60*5) <= System.currentTimeMillis()) {
                        cooldown = System.currentTimeMillis();
                        server.sendConsole("");
                        server.sendConsole("§a----------{ SETUP PANEL START }----------");
                        server.sendConsole("§a1. Create a account at: " + getUrl(URLType.REGISTER));
                        server.sendConsole("§a2. Grant your server panel access at: " + getUrl(URLType.SERVER_ADD));
                        server.sendConsole("");
                        server.sendConsole("§aGUID: " + GUID);
                        server.sendConsole("§aSecret: " + Secret);
                        server.sendConsole("§a----------{ SETUP PANEL END   }----------");
                        server.sendConsole("");
                    }

                    for (MCSQueryData.Response.Task task : data.response.tasks) {
                        if (running.contains(task.id))
                            continue;

                        running.add(task.id);

                        if (task.type != MCSQueryData.TaskType.CREATE_MUTE_REASON && task.type != MCSQueryData.TaskType.CREATE_BAN_REASON) {
                            MCSPlayer target = getPlayer(task.UUID);

                            MCSEntity staff = null;
                            if (task.STAFF.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"))
                                staff = getPlayer(task.STAFF);
                            else
                                staff = new MCSSystem();

                            try {
                                switch (task.type) {
                                    case MUTE:
                                        if (task.reason.text.isEmpty())
                                            target.createMute(staff, getMuteTemplateByID(task.reason.id));
                                        else
                                            target.createCustomMute(staff, task.reason.text, task.expire);
                                        break;
                                    case BAN:
                                        if (task.reason.text.isEmpty())
                                            target.createBan(staff, getBanTemplateByID(task.reason.id));
                                        else
                                            target.createCustomBan(staff, task.reason.text, task.expire);
                                        break;
                                    case GMute:
                                        break;
                                    case GBan:
                                        break;
                                }

                                updateTaskState(task, TaskState.DONE);
                            } catch (IOException | InterruptedException | ExecutionException | SQLException e) {
                                updateTaskState(task, TaskState.FAILED);
                                e.printStackTrace();
                            }
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
    }

    public void end() {
        t.cancel();
    }

    private void updateTaskState(MCSQueryData.Response.Task task, TaskState state) throws InterruptedException, ExecutionException, IOException {
        RequestBuilder rb = getAuthedRequest("task/" + task.id + "/update");
        rb.putParam("state", state.name().toLowerCase());
        rb.post();

        running.remove(task.id);
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
        if (players.containsKey(uuid))
            return new MCSPlayer(players.get(uuid));

        return getPlayer(uuid.toString());
    }
    public MCSPlayer getPlayer(String player) throws IOException, InterruptedException, ExecutionException {
        if (name2UUID.containsKey(player) && players.containsKey(name2UUID.get(player)))
            return new MCSPlayer(players.get(name2UUID.get(player)));

        RequestBuilder rb = new RequestBuilder(API_SERVER + GUID + "/player/" + player + "/details");
        rb.putHeader("Auth-Secret", Secret);
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

    public MuteTemplate creaeteMuteTemplate() {
        return null;
    }

    public MuteTemplate getMuteTemplateByID(String id) throws SQLException {
        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__mutes-templates` WHERE `id`=? LIMIT 1", Arrays.asList(id));
        if (!rs.next())
            return null;
        return new MuteTemplate(rs);
    }
    public MuteTemplate getMuteTemplateByName(String name) throws SQLException {
        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__mutes-templates` WHERE `name`=? LIMIT 1", Arrays.asList(name));
        if (!rs.next())
            return null;
        return new MuteTemplate(rs);
    }
    public MuteTemplate[] getMuteTemplatesByName(String name) throws SQLException {
        List<MuteTemplate> templates = new ArrayList<>();

        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__mutes-templates` WHERE `name` LIKE ?", Arrays.asList(name + "%"));
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

        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__mutes-templates` WHERE `name` LIKE ? && `power`<=?", Arrays.asList(name + "%", power));
        while (rs.next())
            templates.add(new MuteTemplate(rs));

        MuteTemplate[] tp = new MuteTemplate[templates.size()];
        templates.toArray(tp);
        return tp;
    }
    public MuteTemplate getMuteTemplateByPower(int minPower) throws SQLException {
        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__mutes-templates` WHERE `power`<=? LIMIT 1", Arrays.asList(minPower));
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


    public BanTemplate getBanTemplateByID(String id) throws SQLException {
        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__bans-templates` WHERE `id`=? LIMIT 1", Arrays.asList(id));
        if (!rs.next())
            return null;
        return new BanTemplate(rs);
    }
    public BanTemplate getBanTemplateByName(String name) throws SQLException {
        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__bans-templates` WHERE `name`=? LIMIT 1", Arrays.asList(name));
        if (!rs.next())
            return null;
        return new BanTemplate(rs);
    }
    public BanTemplate[] getBanTemplatesByName(String name) throws SQLException {
        List<BanTemplate> templates = new ArrayList<>();

        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__bans-templates` WHERE `name` LIKE ?", Arrays.asList(name + "%"));
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

        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__bans-templates` WHERE `name` LIKE ? && `power`<=?", Arrays.asList(name + "%", power));
        while (rs.next())
            templates.add(new BanTemplate(rs));

        BanTemplate[] tp = new BanTemplate[templates.size()];
        templates.toArray(tp);
        return tp;
    }
    public BanTemplate getBanTemplateByPower(int minPower) throws SQLException {
        ResultSet rs = mysql.query("SELECT * FROM `MCSCore__bans-templates` WHERE `power`<=? LIMIT 1", Arrays.asList(minPower));
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

    public String getUrl(URLType type) {
        if (!urls.containsKey(type))
            return null;

        return urls.get(type);
    }
    public enum URLType {
        REGISTER,
        LOGIN,
        SERVER_ADD,
        SERVER_PANEL;
    }

    private enum TaskState {
        DONE,
        FAILED;
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
