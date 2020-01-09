package net.mcstats2.core.api.MCSEntity;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.chat.BaseComponent;
import net.mcstats2.core.api.config.Configuration;
import net.mcstats2.core.exceptions.MCSError;
import net.mcstats2.core.network.web.RequestResponse;
import net.mcstats2.core.network.web.data.MCSPlayerData;
import net.mcstats2.core.network.web.RequestBuilder;
import net.mcstats2.core.utils.StringUtils;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class MCSPlayer implements MCSEntity {
    private MCSPlayerData data;

    public MCSPlayer(MCSPlayerData data) {
        this.data = data;
    }

    public UUID getUUID() {
        return UUID.fromString(data.response.UUID);
    }

    public String getName() {
        return data.response.name;
    }

    public MCSPlayerData.Response.Name[] getNames() {
        return data.response.names;
    }

    public Skin getSkin() {
        return new Skin(data.response.skin);
    }

    public Session getSession() {
        return new Session(data.response.session);
    }

    public String getLanguage() {
        return getSession().getAddressDetails().getLanguage();
    }


    public boolean createCData(MCSPlayerData.CDataType type, String key, Object value, int expires) {
        try {
            RequestBuilder rb = MCSCore.getInstance().getAuthedRequest("/player/" + getUUID().toString() + "/cdata/create");

            rb.putParam("type", type.name());
            rb.putParam("key", key);
            rb.putParam("value", value);
            rb.putParam("expires", expires);

            RequestResponse rr = rb.post();

            return rr.getStatusCode() == 200;
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return false;
    }
    public CData[] getCDatas() {
        ArrayList<CData> all = new ArrayList<>();

        for (MCSPlayerData.Response.CData datas : data.response.cdata)
            all.add(new CData(datas));

        return all.toArray(new CData[0]);
    }
    public CData[] getCDatas(MCSPlayerData.CDataType type) {
        ArrayList<CData> all = new ArrayList<>();

        for (MCSPlayerData.Response.CData datas : data.response.cdata)
            if (datas.type.equals(type))
                all.add(new CData(datas));

        return all.toArray(new CData[0]);
    }
    public CData getCDataByID(String id) {
        for (CData cData : getCDatas())
            if (cData.getID().equals(id))
                return cData;

        return null;
    }
    public CData getCDataByID(MCSPlayerData.CDataType type, String id) {
        for (CData cData : getCDatas(type))
            if (cData.getID().equals(id))
                return cData;

        return null;
    }
    public CData getCDataByKey(String key) {
        for (CData cData : getCDatas())
            if (cData.getKey().equals(key))
                return cData;

        return null;
    }
    public CData getCDataByKey(MCSPlayerData.CDataType type, String key) {
        if (key == null)
            return null;

        for (CData cData : getCDatas(type)) {
            if (cData.getKey() == null)
                continue;

            if (cData.getKey().equals(key))
                return cData;
        }

        return null;
    }


    public boolean isOnline() {
        return MCSCore.getInstance().getServer().isOnline(this);
    }

    @Override
    public void sendMessage(BaseComponent s) {
        MCSCore.getInstance().getServer().sendMessage(this, s);
    }

    @Override
    public void sendMessage(BaseComponent[] s) {
        MCSCore.getInstance().getServer().sendMessage(this, s);
    }

    public void sendTitle(Integer fadeIn, Integer stay, Integer fadeOut, String title, String subTitle) {
        MCSCore.getInstance().getServer().sendTitle(this, fadeIn, stay, fadeOut, title, subTitle);
    }

    public void sendActionBar(String message, int duration) {
        MCSCore.getInstance().getServer().sendActionBar(this, message, duration);
    }

    public void playSound(String sound, float volume, float pitch) {
        MCSCore.getInstance().getServer().playSound(this, sound, volume, pitch);
    }

    public boolean hasPermission(String s) {
        return MCSCore.getInstance().getServer().hasPermission(this, s);
    }

    @Override
    public Configuration getLang() {
        return MCSCore.getInstance().getLang(getLanguage());
    }

    public int getMax(String path) {
        int limit = 255;

        if (hasPermission("*"))
            return -1;

        if (hasPermission("MCStatsNET.*"))
            return -1;

        if (hasPermission(path + (path.endsWith(".") ? "" : ".") + "*"))
            return -1;

        for (int i=limit;i>0;i--)
            if (hasPermission(path + (path.endsWith(".") ? "" : ".") + i))
                return i;

        return 0;
    }

    public Configuration getLanguageConfig() {
        return MCSCore.getInstance().getLang(getLanguage());
    }

    public void disconnect(String reason) {
        MCSCore.getInstance().getServer().disconnect(this, reason);
    }
    private void disconnect(Ban ban) {
        HashMap<String, Object> replace = new HashMap<>();
        replace.put("id", ban.getID());
        replace.put("reason", ban.getCustomReason() == null ? (ban.getReason() != null ? ban.getReason().getText() : "err") : (ban.getCustomReason().isEmpty() ? "&8&o<none>&r" : ban.getCustomReason()));

        if (ban.getExpire() != 0) {
            HashMap<String, Object> expires = new HashMap<>();
            long endsIn = Math.abs(ban.getExpire());
            long seconds = (endsIn) % 60;
            long minutes = (endsIn / 60) % 60;
            long hours = (endsIn / 60 / 60) % 24;
            long days = (endsIn / 60 / 60 / 24);
            expires.put("seconds", seconds);
            expires.put("minutes", minutes);
            expires.put("hours", hours);
            expires.put("days", days);

            Timestamp end_timestamp = new Timestamp(ban.getTime() + (ban.getExpire()*1000));
            expires.put("end_year", end_timestamp.getYear());
            expires.put("end_month", end_timestamp.getMonth());
            expires.put("end_date", end_timestamp.getDate());
            expires.put("end_day", end_timestamp.getDay());
            expires.put("end_hours", end_timestamp.getHours());
            expires.put("end_minutes", end_timestamp.getMinutes());
            expires.put("end_seconds", end_timestamp.getSeconds());

            replace.put("expires", StringUtils.replace(getLang().getString("expires.temporary"), expires));
        } else
            replace.put("expires", getLang().getString("expires.never"));

        disconnect(MCSCore.getInstance().buildScreen(getLang(), "ban.screen", replace));
    }

    public Warn createWarn(MCSEntity staff, String type, String expire) throws SQLException {
        String id = StringUtils.randomString(10);

        if (MCSCore.getInstance().getMySQL().queryUpdate("INSERT INTO `MCSCore__warns`(`id`, `UUID`, `STAFF`, `type`, `expire`) VALUES (?,?,?,?,?)", id, getUUID().toString(), staff.getUUID() == null ? staff.getName() : staff.getUUID().toString(), type, expire) != 0)
            return getWarn(id);

        return null;
    }

    public Warn getWarn(String id) throws SQLException {
        ResultSet rs = MCSCore.getInstance().getMySQL().query("SELECT * FROM `MCSCore__warns` WHERE `UUID`=? && `id`=? LIMIT 1", getUUID().toString(), id);
        if (rs.next())
            return new Warn(rs);

        return null;
    }
    public List<Warn> getWarns() throws SQLException {
        List<Warn> warns = new ArrayList<>();

        ResultSet rs = MCSCore.getInstance().getMySQL().query("SELECT * FROM `MCSCore__warns` WHERE `UUID`=?", getUUID().toString());
        while (rs.next())
            warns.add(new Warn(rs));

        return warns;
    }
    public List<Warn> getWarnsByType(String type) throws SQLException {
        List<Warn> warns = new ArrayList<>();

        ResultSet rs = MCSCore.getInstance().getMySQL().query("SELECT * FROM `MCSCore__warns` WHERE `UUID`=? && `type`=?", getUUID().toString(), type);
        while (rs.next())
            warns.add(new Warn(rs));

        return warns;
    }


    public Mute createMute(MCSEntity staff, MCSCore.MuteTemplate t) throws SQLException, InterruptedException, ExecutionException, IOException {
        int count = countMutes(t, false);
        if (t.getExpires().size() <= count)
            count = t.getExpires().size() - 1;

        String id = StringUtils.randomString(10);
        int expire = t.getExpires().get(count);

        if (MCSCore.getInstance().getMySQL().queryUpdate("INSERT INTO `MCSCore__mutes`(`id`, `UUID`, `STAFF`, `reason`, `expire`) VALUES (?,?,?,?,?)", id, getUUID().toString(), staff.getUUID() == null ? staff.getName() : staff.getUUID().toString(), t.getID(), expire) != 0) {
            Mute mute = getMute(id);

            HashMap<String, Object> replace = new HashMap<>();
            replace.put("id", mute.getID());
            replace.put("reason", mute.getCustomReason() == null ? (mute.getReason() != null ? mute.getReason().getText() : "err") : (mute.getCustomReason().isEmpty() ? "&8&o<none>&r" : mute.getCustomReason()));

            if (mute.getExpire() != 0) {
                HashMap<String, Object> expires = new HashMap<>();
                long endsIn = Math.abs(mute.getExpire());
                long seconds = (endsIn) % 60;
                long minutes = (endsIn / 60) % 60;
                long hours = (endsIn / 60 / 60) % 24;
                long days = (endsIn / 60 / 60 / 24);
                expires.put("seconds", seconds);
                expires.put("minutes", minutes);
                expires.put("hours", hours);
                expires.put("days", days);

                Timestamp end_timestamp = new Timestamp(mute.getTime() + (mute.getExpire()*1000));
                expires.put("end_year", end_timestamp.getYear());
                expires.put("end_month", end_timestamp.getMonth());
                expires.put("end_date", end_timestamp.getDate());
                expires.put("end_day", end_timestamp.getDay());
                expires.put("end_hours", end_timestamp.getHours());
                expires.put("end_minutes", end_timestamp.getMinutes());
                expires.put("end_seconds", end_timestamp.getSeconds());

                replace.put("expires", StringUtils.replace(getLang().getString("expires.temporary"), expires));
            } else
                replace.put("expires", getLang().getString("expires.never"));

            if (isOnline())
                sendMessage(MCSCore.getInstance().buildScreen(getLanguageConfig(), "mute.screen", replace));

            replace.put("playername", getName());
            replace.put("staffname", staff.getName());
            MCSCore.getInstance().broadcast("MCStatsNET.mute.alert", "mute.prefix", "mute.alert", replace);

            return mute;
        }

        return null;
    }
    public Mute createCustomMute(MCSEntity staff, String text, int expire) throws SQLException, InterruptedException, ExecutionException, IOException {
        String id = StringUtils.randomString(10);

        if (text == null)
            text = "";

        if (MCSCore.getInstance().getMySQL().queryUpdate("INSERT INTO `MCSCore__mutes`(`id`, `UUID`, `STAFF`, `reason-text`, `expire`) VALUES (?,?,?,?,?)", id, getUUID().toString(), staff.getUUID() == null ? staff.getName() : staff.getUUID().toString(), text, expire) != 0) {
            Mute mute = getMute(id);

            HashMap<String, Object> replace = new HashMap<>();
            replace.put("id", mute.getID());
            replace.put("reason", mute.getCustomReason() == null ? (mute.getReason() != null ? mute.getReason().getText() : "err") : (mute.getCustomReason().isEmpty() ? "&8&o<none>&r" : mute.getCustomReason()));

            if (mute.getExpire() != 0) {
                HashMap<String, Object> expires = new HashMap<>();
                long endsIn = Math.abs(mute.getExpire());
                long seconds = (endsIn) % 60;
                long minutes = (endsIn / 60) % 60;
                long hours = (endsIn / 60 / 60) % 24;
                long days = (endsIn / 60 / 60 / 24);
                expires.put("seconds", seconds);
                expires.put("minutes", minutes);
                expires.put("hours", hours);
                expires.put("days", days);

                Timestamp end_timestamp = new Timestamp(mute.getTime() + (mute.getExpire()*1000));
                expires.put("end_year", end_timestamp.getYear());
                expires.put("end_month", end_timestamp.getMonth());
                expires.put("end_date", end_timestamp.getDate());
                expires.put("end_day", end_timestamp.getDay());
                expires.put("end_hours", end_timestamp.getHours());
                expires.put("end_minutes", end_timestamp.getMinutes());
                expires.put("end_seconds", end_timestamp.getSeconds());

                replace.put("expires", StringUtils.replace(getLang().getString("expires.temporary"), expires));
            } else
                replace.put("expires", getLang().getString("expires.never"));

            if (isOnline())
                sendMessage(MCSCore.getInstance().buildScreen(getLanguageConfig(), "mute.screen", replace));

            replace.put("playername", getName());
            replace.put("staffname", staff.getName());
            MCSCore.getInstance().broadcast("MCStatsNET.mute.alert", "mute.prefix", "mute.alert", replace);

            return mute;
        }

        return null;
    }

    public Mute getMute(String id) throws SQLException, InterruptedException, ExecutionException, IOException {
        ResultSet rs = MCSCore.getInstance().getMySQL().query("SELECT * FROM `MCSCore__mutes` WHERE `UUID`=? && `id`=? LIMIT 1", getUUID().toString(), id);
        if (rs.next())
            return new Mute(rs);

        return null;
    }
    public Mute getActiveMute() throws SQLException, InterruptedException, ExecutionException, IOException {
        ResultSet rs = MCSCore.getInstance().getMySQL().query("SELECT * FROM `MCSCore__mutes` WHERE `UUID`=? && `valid`=1 && (DATE_ADD(`timestamp`,INTERVAL `expire` SECOND) >= NOW() || `expire` = 0 || `expire` IS NULL) LIMIT 1", getUUID().toString());
        if (rs.next())
            return new Mute(rs);

        return null;
    }
    public List<Mute> getMutes() throws SQLException, InterruptedException, ExecutionException, IOException {
        List<Mute> mutes = new ArrayList<>();

        ResultSet rs = MCSCore.getInstance().getMySQL().query("SELECT * FROM `MCSCore__mutes` WHERE `UUID`=?", getUUID().toString());
        while (rs.next()) {
            mutes.add(new Mute(rs));
        }

        return mutes;
    }
    public int countMutes(MCSCore.MuteTemplate reason, boolean onlyActives) throws SQLException {
        ResultSet rs = MCSCore.getInstance().getMySQL().query("SELECT COUNT(*) as 'amount' FROM `MCSCore__mutes` WHERE `UUID`=? && `reason`=?" + (onlyActives ? " && `valid`=1 && (DATE_ADD(`timestamp`,INTERVAL `expire` SECOND) >= NOW() || `expire` = 0 || `expire` IS NULL)" : ""), getUUID().toString(), reason == null ? "%" : reason.getID());
        if (rs.next())
            return rs.getInt("amount");

        return 0;
    }
    public List<Mute> getMutes(int limit, int offset) throws SQLException, InterruptedException, ExecutionException, IOException {
        List<Mute> mutes = new ArrayList<>();

        ResultSet rs = MCSCore.getInstance().getMySQL().query("SELECT * FROM `MCSCore__mutes` WHERE `UUID`=? LIMIT ? OFFSET ?", getUUID().toString(), limit, offset);
        while (rs.next()) {
            mutes.add(new Mute(rs));
        }

        return mutes;
    }
    public List<Mute> getMutes(MCSCore.MuteTemplate reason) throws SQLException, InterruptedException, ExecutionException, IOException {
        List<Mute> mutes = new ArrayList<>();

        ResultSet rs = MCSCore.getInstance().getMySQL().query("SELECT * FROM `MCSCore__mutes` WHERE `UUID`=? && `reason`=?", getUUID().toString(), reason.getID());
        while (rs.next()) {
            mutes.add(new Mute(rs));
        }

        return mutes;
    }
    public List<Mute> getMutesBySTAFF(MCSEntity staff) throws SQLException, InterruptedException, ExecutionException, IOException {
        List<Mute> mutes = new ArrayList<>();

        ResultSet rs = MCSCore.getInstance().getMySQL().query("SELECT * FROM `MCSCore__mutes` WHERE `UUID`=? && `STAFF`=?", getUUID().toString(), staff.getUUID() == null ? staff.getName() : staff.getUUID().toString());
        while (rs.next()) {
            mutes.add(new Mute(rs));
        }

        return mutes;
    }


    public Ban createBan(MCSEntity staff, MCSCore.BanTemplate t) throws SQLException, InterruptedException, ExecutionException, IOException {
        int count = countBans(t, false);
        if (t.getExpires().size() <= count)
            count = t.getExpires().size() - 1;

        String id = StringUtils.randomString(10);
        int expire = t.getExpires().get(count);

        if (MCSCore.getInstance().getMySQL().queryUpdate("INSERT INTO `MCSCore__bans`(`id`, `UUID`, `STAFF`, `reason`, `expire`) VALUES (?,?,?,?,?)", id, getUUID().toString(), staff.getUUID() == null ? staff.getName() : staff.getUUID().toString(), t.getID(), expire) != 0) {
            Ban ban = getBan(id);

            HashMap<String, Object> replace = new HashMap<>();
            replace.put("id", ban.getID());
            replace.put("reason", ban.getCustomReason() == null ? (ban.getReason() != null ? ban.getReason().getText() : "err") : (ban.getCustomReason().isEmpty() ? "&8&o<none>&r" : ban.getCustomReason()));

            if (ban.getExpire() != 0) {
                HashMap<String, Object> expires = new HashMap<>();
                long endsIn = Math.abs(ban.getExpire());
                long seconds = (endsIn) % 60;
                long minutes = (endsIn / 60) % 60;
                long hours = (endsIn / 60 / 60) % 24;
                long days = (endsIn / 60 / 60 / 24);
                expires.put("seconds", seconds);
                expires.put("minutes", minutes);
                expires.put("hours", hours);
                expires.put("days", days);

                Timestamp end_timestamp = new Timestamp(ban.getTime() + (ban.getExpire()*1000));
                expires.put("end_year", end_timestamp.getYear());
                expires.put("end_month", end_timestamp.getMonth());
                expires.put("end_date", end_timestamp.getDate());
                expires.put("end_day", end_timestamp.getDay());
                expires.put("end_hours", end_timestamp.getHours());
                expires.put("end_minutes", end_timestamp.getMinutes());
                expires.put("end_seconds", end_timestamp.getSeconds());

                replace.put("expires", StringUtils.replace(getLang().getString("expires.temporary"), expires));
            } else
                replace.put("expires", getLang().getString("expires.never"));

            replace.put("playername", getName());
            replace.put("staffname", staff.getName());
            MCSCore.getInstance().broadcast("MCStatsNET.ban.alert", "ban.prefix", "ban.alert", replace);

            if (isOnline())
                disconnect(ban);

            return ban;
        }

        return null;
    }
    public Ban createCustomBan(MCSEntity staff, String text, int expire) throws SQLException, InterruptedException, ExecutionException, IOException {
        String id = StringUtils.randomString(10);

        if (text == null)
            text = "";

        if (MCSCore.getInstance().getMySQL().queryUpdate("INSERT INTO `MCSCore__bans`(`id`, `UUID`, `STAFF`, `reason-text`, `expire`) VALUES (?,?,?,?,?)", id, getUUID().toString(), staff.getUUID() == null ? staff.getName() : staff.getUUID().toString(),text, expire) != 0) {
            Ban ban = getBan(id);

            HashMap<String, Object> replace = new HashMap<>();
            replace.put("id", ban.getID());
            replace.put("reason", ban.getCustomReason() == null ? (ban.getReason() != null ? ban.getReason().getText() : "err") : (ban.getCustomReason().isEmpty() ? "&8&o<none>&r" : ban.getCustomReason()));

            if (ban.getExpire() != 0) {
                HashMap<String, Object> expires = new HashMap<>();
                long endsIn = Math.abs(ban.getExpire());
                long seconds = (endsIn) % 60;
                long minutes = (endsIn / 60) % 60;
                long hours = (endsIn / 60 / 60) % 24;
                long days = (endsIn / 60 / 60 / 24);
                expires.put("seconds", seconds);
                expires.put("minutes", minutes);
                expires.put("hours", hours);
                expires.put("days", days);

                Timestamp end_timestamp = new Timestamp(ban.getTime() + (ban.getExpire()*1000));
                expires.put("end_year", end_timestamp.getYear());
                expires.put("end_month", end_timestamp.getMonth());
                expires.put("end_date", end_timestamp.getDate());
                expires.put("end_day", end_timestamp.getDay());
                expires.put("end_hours", end_timestamp.getHours());
                expires.put("end_minutes", end_timestamp.getMinutes());
                expires.put("end_seconds", end_timestamp.getSeconds());

                replace.put("expires", StringUtils.replace(getLang().getString("expires.temporary"), expires));
            } else
                replace.put("expires", getLang().getString("expires.never"));

            replace.put("playername", getName());
            replace.put("staffname", staff.getName());
            MCSCore.getInstance().broadcast("MCStatsNET.ban.alert", "ban.prefix", "ban.alert", replace);

            if (isOnline())
                disconnect(ban);

            return ban;
        }

        return null;
    }

    public Ban getBan(String id) throws SQLException, InterruptedException, ExecutionException, IOException {
        ResultSet rs = MCSCore.getInstance().getMySQL().query("SELECT * FROM `MCSCore__bans` WHERE `UUID`=? && `id`=? LIMIT 1", getUUID().toString(), id);
        if (rs.next())
            return new Ban(rs);

        return null;
    }
    public Ban getActiveBan() throws SQLException, InterruptedException, ExecutionException, IOException {
        ResultSet rs = MCSCore.getInstance().getMySQL().query("SELECT * FROM `MCSCore__bans` WHERE `UUID`=? && `valid`=1 && (DATE_ADD(`timestamp`,INTERVAL `expire` SECOND) >= NOW() || `expire` = 0 || `expire` IS NULL) LIMIT 1", getUUID().toString());
        if (rs.next())
            return new Ban(rs);

        return null;
    }
    public List<Ban> getBans() throws SQLException, InterruptedException, ExecutionException, IOException {
        List<Ban> bans = new ArrayList<>();

        ResultSet rs = MCSCore.getInstance().getMySQL().query("SELECT * FROM `MCSCore__bans` WHERE `UUID`=?", getUUID().toString());
        while (rs.next()) {
            bans.add(new Ban(rs));
        }

        return bans;
    }
    public int countBans(MCSCore.BanTemplate reason, boolean onlyActives) throws SQLException {
        ResultSet rs = MCSCore.getInstance().getMySQL().query("SELECT COUNT(*) as 'amount' FROM `MCSCore__bans` WHERE `UUID`=? && `reason`=?" + (onlyActives ? " && `valid`=1 && (DATE_ADD(`timestamp`,INTERVAL `expire` SECOND) >= NOW() || `expire` = 0 || `expire` IS NULL)" : ""), getUUID().toString(), reason == null ? "%" : reason.getID());
        if (rs.next())
            return rs.getInt("amount");

        return 0;
    }
    public List<Ban> getBans(int limit, int offset) throws SQLException, InterruptedException, ExecutionException, IOException {
        List<Ban> bans = new ArrayList<>();

        ResultSet rs = MCSCore.getInstance().getMySQL().query("SELECT * FROM `MCSCore__bans` WHERE `UUID`=? LIMIT ? OFFSET ?", getUUID().toString(), limit, offset);
        while (rs.next()) {
            bans.add(new Ban(rs));
        }

        return bans;
    }
    public List<Ban> getBans(MCSCore.BanTemplate reason) throws SQLException, InterruptedException, ExecutionException, IOException {
        List<Ban> bans = new ArrayList<>();

        ResultSet rs = MCSCore.getInstance().getMySQL().query("SELECT * FROM `MCSCore__bans` WHERE `UUID`=? && `reason`=?", getUUID().toString(), reason.getID());
        while (rs.next()) {
            bans.add(new Ban(rs));
        }

        return bans;
    }
    public List<Ban> getBans(MCSEntity staff) throws SQLException, InterruptedException, ExecutionException, IOException {
        List<Ban> bans = new ArrayList<>();

        ResultSet rs = MCSCore.getInstance().getMySQL().query("SELECT * FROM `MCSCore__bans` WHERE `UUID`=? && `STAFF`=?", getUUID().toString(), staff.getUUID() == null ? staff.getName() : staff.getUUID().toString());
        while (rs.next()) {
            bans.add(new Ban(rs));
        }

        return bans;
    }

    public class Skin {
        private MCSPlayerData.Response.Skin skin;


        private Skin(MCSPlayerData.Response.Skin skin) {
            this.skin = skin;
        }

        public String getID() {
            return skin.id;
        }

        public URLs getURLs() {
            return new URLs(skin.urls);
        }
        public class URLs {
            private MCSPlayerData.Response.Skin.URLs urls;
            private URLs(MCSPlayerData.Response.Skin.URLs urls) {
                urls = this.urls;
            }

            public String mojang() {
                return urls.mojang;
            }

            public String raw() {
                return urls.raw;
            }

            public String head() {
                return urls.head;
            }

            public String body() {
                return urls.body;
            }

            public String render() {
                return urls.render;
            }
        }

        public String getValue() {
            return skin.value;
        }

        public String getSignature() {
            return skin.signature;
        }

        public MCSPlayerData.SkinStatus getStatus() {
            return skin.status;
        }
    }

    public class Session {
        private MCSPlayerData.Response.Session session;

        Session(MCSPlayerData.Response.Session session) {
            this.session =  session;
        }

        public AddressDetails getAddressDetails() {
            return new AddressDetails(session.addressDetails);
        }

        public Checks getChecks() {
            return new Checks(session.checks);
        }

        public class AddressDetails {
            private MCSPlayerData.Response.Session.AddressDetails addressDetails;

            AddressDetails(MCSPlayerData.Response.Session.AddressDetails addressDetails) {
                this.addressDetails = addressDetails;
            }

            public String getContinentCode() {
                return addressDetails.continent_code;
            }

            public String getContinentName() {
                return addressDetails.continent_name;
            }

            public String getCountryIsoCode() {
                return addressDetails.country_iso_code;
            }

            public String getCountryCode() {
                return addressDetails.country_name;
            }

            public String getLanguage() {
                return addressDetails.language;
            }

            public String getTimezone() {
                return addressDetails.timezone;
            }

            public boolean is_in_european_union() {
                return addressDetails.is_in_european_union;
            }
        }

        public class Checks {
            private MCSPlayerData.Response.Session.Checks checks;

            Checks(MCSPlayerData.Response.Session.Checks checks) {
                this.checks = checks;
            }

            public Name getName() {
                if (checks.name == null)
                    return null;

                return new Name(checks.name);
            }

            public VPN getVPN() {
                if (checks.vpn == null)
                    return null;

                return new VPN(checks.vpn);
            }

            public GMute getGMute() {
                if (checks.gmute == null)
                    return null;

                return new GMute(checks.gmute);
            }

            public GBan getGBan() {
                if (checks.gban == null)
                    return null;

                return new GBan(checks.gban);
            }

            public class Name {
                MCSPlayerData.Response.Session.Checks.Name name;


                Name(MCSPlayerData.Response.Session.Checks.Name name) {
                    this.name = name;
                }

                public String getID() {
                    return name.id;
                }

                public String getReason() {
                    return name.reason;
                }

                public boolean isBlocked() {
                    return name.block;
                }
            }

            public class VPN {
                MCSPlayerData.Response.Session.Checks.VPN vpn;


                VPN(MCSPlayerData.Response.Session.Checks.VPN vpn) {
                    this.vpn = vpn;
                }

                public String getID() {
                    return vpn.id;
                }

                public boolean isBlocked() {
                    return vpn.block;
                }
            }

            public class GMute {
                MCSPlayerData.Response.Session.Checks.GMute gmute;


                GMute(MCSPlayerData.Response.Session.Checks.GMute gmute) {
                    this.gmute = gmute;
                }

                public String getScreen() {
                    return gmute.screen;
                }

                public String getAlert() {
                    return gmute.alert;
                }

                public String getID() {
                    return gmute.id;
                }

                public String getSTAFF() {
                    return gmute.STAFF;
                }

                public MCSPlayerData.Response.Session.Checks.Reason getReason() {
                    return gmute.reason;
                }

                public MCSPlayerData.Response.Session.Checks.Proof[] getProofs() {
                    return gmute.proofs;
                }

                public int getExpire() {
                    return gmute.expire;
                }

                public String getTimestamp() {
                    return gmute.timestamp;
                }
            }

            public class GBan {
                MCSPlayerData.Response.Session.Checks.GBan gban;

                GBan(MCSPlayerData.Response.Session.Checks.GBan gban) {
                    this.gban = gban;
                }

                public String getScreen() {
                    return gban.screen;
                }

                public String getAlert() {
                    return gban.alert;
                }

                public String getID() {
                    return gban.id;
                }

                public String getSTAFF() {
                    return gban.STAFF;
                }

                public MCSPlayerData.Response.Session.Checks.Reason getReason() {
                    return gban.reason;
                }

                public MCSPlayerData.Response.Session.Checks.Proof[] getProofs() {
                    return gban.proofs;
                }

                public int getExpire() {
                    return gban.expire;
                }

                public String getTimestamp() {
                    return gban.timestamp;
                }
            }
        }
    }

    public class CData {
        private MCSPlayerData.Response.CData cdata;

        CData(MCSPlayerData.Response.CData cdata) {
            this.cdata = cdata;

            if (getExpires() != 0)
                MCSCore.getInstance().getTimer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            MCSCore.getInstance().getPlayer(getUUID(), true);
                        } catch (IOException | InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                }, StringUtils.timestampAddSeconds(StringUtils.string2Timestamp(getTimestamp()), getExpires()));

        }

        public String getID() {
            return cdata.id;
        }

        public MCSPlayerData.CDataType getType() {
            return cdata.type;
        }

        public String getKey() {
            return cdata.key;
        }

        public Object getValue() {
            return cdata.value;
        }

        public int getExpires() {
            return cdata.expires;
        }

        public String getTimestamp() {
            return cdata.timestamp;
        }

        public boolean setValue(Object newValue) throws InterruptedException, ExecutionException, IOException {
            RequestBuilder rb = MCSCore.getInstance().getAuthedRequest("/player/" + getUUID().toString() + "/cdata/" + getID() + "/update");

            rb.putParam("value", newValue);

            RequestResponse rr = rb.post();

            boolean status = rr.getStatusCode() == 200;

            if (status)
                cdata.value = newValue;
            else
                try {
                    throw new MCSError(rr.getStatusLine().getReasonPhrase());
                } catch (MCSError mcsError) {
                    mcsError.printStackTrace();
                }

            return status;
        }
    }

    public class Warn {

        Warn(ResultSet rs) {
        }
    }

    public class Mute {
        private String id;
        private String MCSid;
        private MCSEntity STAFF;
        private MCSCore.MuteTemplate reason;
        private String customReason;
        private int expire;
        private boolean valid;
        private long time;
        private String timestamp;

        Mute(ResultSet rs) throws SQLException, InterruptedException, ExecutionException, IOException {
            id = rs.getString("id");
            MCSid = rs.getString("MCSid");
            STAFF = rs.getString("STAFF").equals("CONSOLE") ? new MCSConsole() : MCSCore.getInstance().getPlayer(UUID.fromString(rs.getString("STAFF")));
            reason = rs.getString("reason") != null && !rs.getString("reason").isEmpty() ? MCSCore.getInstance().getMuteTemplateByID(rs.getString("reason")) : null;
            customReason = rs.getString("reason-text");
            expire = rs.getInt("expire");
            valid = rs.getBoolean("valid");
            time = rs.getTimestamp("timestamp").getTime();
            timestamp = rs.getString("timestamp");
        }

        public String getID() {
            return id;
        }

        public String getMCSid() {
            return MCSid;
        }

        public MCSEntity getSTAFF() {
            return STAFF;
        }

        public MCSCore.MuteTemplate getReason() {
            if (reason == null)
                return null;

            return reason;
        }

        public String getCustomReason() {
            return customReason;
        }

        public int getExpire() {
            return expire;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > getTime() + (getExpire() * 1000);
        }

        public boolean isValid() {
            return valid;
        }

        public long getTime() {
            return time;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public boolean delete() {
            return MCSCore.getInstance().getMySQL().queryUpdate("UPDATE MCSCore__mutes SET valid=0 WHERE id=? && UUID=?", getID(), getUUID().toString()) != 0;
        }
    }
    public class Ban {
        private String id;
        private String MCSid;
        private MCSEntity STAFF;
        private MCSCore.BanTemplate reason;
        private String customReason;
        private int expire;
        private boolean valid;
        private long time;
        private String timestamp;

        Ban(ResultSet rs) throws SQLException, InterruptedException, ExecutionException, IOException {
            id = rs.getString("id");
            MCSid = rs.getString("MCSid");
            STAFF = rs.getString("STAFF").equals("CONSOLE") ? new MCSConsole() : MCSCore.getInstance().getPlayer(UUID.fromString(rs.getString("STAFF")));
            reason = rs.getString("reason") != null && !rs.getString("reason").isEmpty() ? MCSCore.getInstance().getBanTemplateByID(rs.getString("reason")) : null;
            customReason = rs.getString("reason-text");
            expire = rs.getInt("expire");
            valid = rs.getBoolean("valid");
            time = rs.getTimestamp("timestamp").getTime();
            timestamp = rs.getString("timestamp");
        }

        public String getID() {
            return id;
        }

        public String getMCSid() {
            return MCSid;
        }

        public MCSEntity getSTAFF() {
            return STAFF;
        }

        public MCSCore.BanTemplate getReason() {
            if (reason == null)
                return null;

            return reason;
        }

        public String getCustomReason() {
            return customReason;
        }

        public int getExpire() {
            return expire;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > getTime() + (getExpire() * 1000);
        }

        public boolean isValid() {
            return valid;
        }

        public long getTime() {
            return time;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public boolean delete() {
            return MCSCore.getInstance().getMySQL().queryUpdate("UPDATE MCSCore__bans SET valid=0 WHERE id=? && UUID=?", getID(), getUUID().toString()) != 0;
        }
    }
}

