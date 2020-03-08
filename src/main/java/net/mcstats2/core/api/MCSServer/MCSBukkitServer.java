package net.mcstats2.core.api.MCSServer;

import cloud.timo.TimoCloud.api.TimoCloudAPI;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.dytanic.cloudnet.api.CloudAPI;
import de.dytanic.cloudnet.lib.server.ServerGroupMode;
import net.mcstats2.bridge.server.bukkit.MCPerms;
import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.api.MCSEntity.MCSConsole;
import net.mcstats2.core.api.MCSEntity.MCSEntity;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.mcstats2.core.api.chat.BaseComponent;
import net.mcstats2.core.api.chat.serializer.ComponentSerializer;
import net.mcstats2.core.api.config.Configuration;
import net.mcstats2.core.modules.chatlog.data.ChatLogDataCommand;
import net.mcstats2.core.modules.chatlog.data.ChatLogDataMessage;
import net.mcstats2.core.network.web.RequestBuilder;
import net.mcstats2.core.network.web.RequestResponse;
import net.mcstats2.core.network.web.data.MCSPlayerData;
import net.mcstats2.core.utils.server.bukkit.ActionBarAPI;
import net.mcstats2.core.utils.server.bukkit.TitleAPI;
import net.mcstats2.core.utils.server.bukkit.tinyprotocol.Reflection;
import net.mcstats2.core.utils.server.bukkit.tinyprotocol.TinyProtocol;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import us.myles.ViaVersion.api.Via;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class MCSBukkitServer implements MCSServer, Listener {
    private Plugin plugin;
    private Property replaceSkin;

    private TinyProtocol protocol;

    private Class<?> craftplayer = Reflection.getClass("{obc}.entity.CraftPlayer");
    private Reflection.MethodInvoker getHandle = Reflection.getMethod(craftplayer, "getHandle");
    private Class<?> ep = Reflection.getClass("{nms}.EntityPlayer");

    Class<?> ms = Reflection.getClass("{nms}.MinecraftServer");
    Reflection.FieldAccessor<?> msf = Reflection.getField(ep, ms, 0);
    Class<?> serverpingclazz = Reflection.getClass("{nms}.ServerPing");
    Reflection.FieldAccessor<?> sp_ = Reflection.getField(ms, serverpingclazz, 0);
    Class<?> serverdataclazz = Reflection.getClass("{nms}.ServerPing$ServerData");
    Reflection.FieldAccessor<?> sd_ = Reflection.getField(serverpingclazz, serverdataclazz, 0);
    Reflection.MethodInvoker gpv = Reflection.getMethod(serverdataclazz, "getProtocolVersion");

    Reflection.FieldAccessor<GameProfile> gp_ = Reflection.getField(ep, GameProfile.class, 0);
    //Class<?> PlayerList = Reflection.getClass("{nms}.PlayerList");
    //Reflection.FieldAccessor<?> getPlayerList = Reflection.getField(ms, PlayerList, 0);
    //Class<?> DimensionManager = Reflection.getClass("{nms}.DimensionManager");
    //Reflection.FieldAccessor<?> dm = Reflection.getField(ep, DimensionManager, 0);


    public MCSBukkitServer(Plugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);


        protocol = new TinyProtocol(plugin) {};
        replaceSkin = getSkin(plugin.getConfig().getString("Modules.SkinChecker.auto-change-skin.replacement"));
    }

    @EventHandler
    public void on(PlayerLoginEvent e) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Player pp = e.getPlayer();

                System.out.println(pp.getName() + "[" + pp.getUniqueId().toString() + "] - Fetching MCSProfile...");

                Object ep_ = null;
                int version = -1;
                try {
                    ep_ = getHandle.invoke(pp);

                    if (plugin.getServer().getPluginManager().isPluginEnabled("ViaVersion")) {
                        version = Via.getAPI().getPlayerVersion(pp.getUniqueId());

                    } else {
                        Object msf_ = msf.get(ep_);
                        Object sp = sp_.get(msf_);
                        Object sd = sd_.get(sp);

                        version = Integer.getInteger(gpv.invoke(sd).toString());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }


                MCSPlayer player = null;
                try {
                    player = MCSCore.getInstance().playerJoin(pp.getUniqueId(),
                            pp.getName(),
                            e.getAddress().getHostAddress(),
                            e.getHostname(),
                            version);
                } catch (IOException | InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                }

                if (player == null) {
                    Bukkit.getScheduler().runTask(plugin, () -> pp.kickPlayer("§cThere was an Error with your profile!"));
                    return;
                }

                MCSPlayer.Session session = player.getSession();
                MCSPlayer.Session.AddressDetails addressDetails = session.getAddressDetails();

                Configuration lang = MCSCore.getInstance().getLang(addressDetails.getLanguage());

                if (player.getActiveBan() != null) {
                    MCSPlayer.Ban ban = player.getActiveBan();

                    HashMap<String, Object> replace = new HashMap<>();
                    replace.put("id", ban.getID());
                    replace.put("reason", ban.getCustomReason() == null ? (ban.getReason() != null ? ban.getReason().getText() : "err") : (ban.getCustomReason().isEmpty() ? "&8&o<none>&r" : ban.getCustomReason()));

                    if (ban.getExpire() != 0) {
                        long endsIn = Math.abs((System.currentTimeMillis() / 1000) - (ban.getExpire() + (ban.getTime() / 1000)));
                        long seconds = (endsIn) % 60;
                        long minutes = (endsIn / 60) % 60;
                        long hours = (endsIn / 60 / 60) % 24;
                        long days = (endsIn / 60 / 60 / 24);
                        replace.put("seconds", seconds);
                        replace.put("minutes", minutes);
                        replace.put("hours", hours);
                        replace.put("days", days);
                    }

                    player.disconnect(MCSCore.getInstance().buildScreen(lang, ban.getExpire() != 0 ? "ban.temp.screen" : "ban.perm.screen", replace));
                    return;
                }

                if (session.getChecks() != null) {
                    MCSPlayer.Session.Checks checks = session.getChecks();

                    if (plugin.getConfig().getBoolean("Modules.gBan.enabled") && checks.getGBan() != null) {
                        if (!pp.hasPermission("MCStatsNET.gban.bypass")) {
                            MCSCore.getInstance().getServer().sendConsole((ChatColor.translateAlternateColorCodes('&', checks.getGBan().getAlert())));
                            for (Player pp1 : plugin.getServer().getOnlinePlayers())
                                if (pp1.hasPermission("MCStatsNET.gban.alert"))
                                    pp1.sendMessage((ChatColor.translateAlternateColorCodes('&', checks.getGBan().getAlert())));

                            player.disconnect((ChatColor.translateAlternateColorCodes('&', checks.getGBan().getScreen())));

                            return;
                        }
                    }

                    if (plugin.getConfig().getBoolean("Modules.SkinChecker.enabled") && player.getSkin() != null && player.getSkin().getStatus().equals(MCSPlayerData.SkinStatus.BLACKLISTED)) {
                        if (!pp.hasPermission("MCStatsNET.SkinChecker.bypass")) {
                            HashMap<String, Object> replace = new HashMap<>();
                            replace.put("id", player.getSkin().getID());
                            replace.put("playername", pp.getName());
                            MCSCore.getInstance().broadcast("MCStatsNET.SkinChecker.alert", "checks.prefix", "checks.SkinChecker.alert", replace);

                            if (plugin.getConfig().getBoolean("Modules.SkinChecker.auto-ban.enabled")) {
                                if (!pp.hasPermission("MCStatsNET.ban.bypass"))
                                    player.createCustomBan(new MCSConsole(), plugin.getConfig().getString("Modules.SkinChecker.auto-ban.reason"), plugin.getConfig().getInt("Modules.SkinChecker.auto-ban.expire"));
                            } else {
                                if (plugin.getConfig().getBoolean("Modules.SkinChecker.auto-change-skin.enabled") && replaceSkin != null) {
                                    try {
                                        if (gp_.hasField(ep_)) {
                                            GameProfile gp = gp_.get(ep_);

                                            gp.getProperties().clear();
                                            gp.getProperties().put("textures", replaceSkin);
                                            gp_.set(ep_, gp);


                                                Reflection.MethodInvoker gei = Reflection.getMethod(Reflection.getClass("{obc}.entity.CraftEntity"), "getEntityId");
                                                Reflection.ConstructorInvoker PacketPlayOutEntityDestroyCon = Reflection.getConstructor("{nms}.PacketPlayOutEntityDestroy");
                                                System.out.println(gei.invoke(ep_));
                                                Object packetDestroyPlayer = PacketPlayOutEntityDestroyCon.invoke((int)gei.invoke(ep_));
                                                Class<?> infoActionClass = Reflection.getClass("{nms}.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
                                                Reflection.ConstructorInvoker PacketPlayOutPlayerInfoCon = Reflection.getConstructor("{nms}.PacketPlayOutPlayerInfo", infoActionClass);
                                                Object packetRemovePlayer = PacketPlayOutPlayerInfoCon.invoke(infoActionClass.getEnumConstants()[4], ep_);

                                                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                                    Bukkit.getOnlinePlayers().forEach(pall -> {
                                                        protocol.sendPacket(pall, packetRemovePlayer);
                                                        if (pall != pp)
                                                            protocol.sendPacket(pall, packetDestroyPlayer);
                                                    });
                                                }, 1);

                                            //CraftPlayer craftPlayer = (CraftPlayer) pp;

                                            /*PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(craftPlayer.getEntityId());
                                            PacketPlayOutPlayerInfo tabRemove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, craftPlayer.getHandle());

                                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                                craftPlayer.getHandle().server.getPlayerList().moveToWorld(craftPlayer.getHandle(), craftPlayer.getHandle().dimension, false, pp.getLocation(), true);
                                                for (Player all : Bukkit.getOnlinePlayers()) {
                                                    CraftPlayer craftAll = (CraftPlayer) all;
                                                    craftAll.getHandle().playerConnection.sendPacket(tabRemove);
                                                    if (!all.equals(pp)) {
                                                        craftAll.getHandle().playerConnection.sendPacket(destroy);
                                                    }
                                                }
                                            }, 1);*/

                                            //PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(craftPlayer.getHandle());
                                            //PacketPlayOutPlayerInfo tabAdd = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, craftPlayer.getHandle());

                                            /*Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                                for (Player all : Bukkit.getOnlinePlayers()) {
                                                    CraftPlayer craftAll = (CraftPlayer) all;
                                                    craftAll.getHandle().playerConnection.sendPacket(tabAdd);
                                                    if (!all.equals(pp)) {
                                                        craftAll.getHandle().playerConnection.sendPacket(spawn);
                                                    }
                                                }
                                            }, 20);*/
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        replace = new HashMap<>();
                                        replace.put("id", player.getSkin().getID());
                                        replace.put("playername", pp.getName());
                                        player.disconnect(MCSCore.getInstance().buildScreen(lang, "checks.SkinChecker.screen", replace));
                                    }

                                } else {
                                    replace = new HashMap<>();
                                    replace.put("id", player.getSkin().getID());
                                    replace.put("playername", pp.getName());
                                    player.disconnect(MCSCore.getInstance().buildScreen(lang, "checks.SkinChecker.screen", replace));
                                }
                            }
                            return;

                        } else {
                            HashMap<String, Object> replace = new HashMap<>();
                            replace.put("id", player.getSkin().getID());
                            replace.put("playername", pp.getName());
                            MCSCore.getInstance().broadcast("MCStatsNET.SkinChecker.alert", "checks.prefix", "checks.SkinChecker.alertButIgnore", replace);
                        }
                    }

                    if (plugin.getConfig().getBoolean("Modules.AntiVPN.enabled") && checks.getVPN() != null && checks.getVPN().isBlocked()) {
                        if (!pp.hasPermission("MCStatsNET.AntiVPN.bypass")) {
                            HashMap<String, Object> replace = new HashMap<>();
                            replace.put("id", checks.getVPN().getID());
                            replace.put("playername", pp.getName());
                            MCSCore.getInstance().broadcast("MCStatsNET.AntiVPN.alert", "checks.prefix", "checks.AntiVPN.alert", replace);

                            if (plugin.getConfig().getBoolean("Modules.AntiVPN.auto-ban.enabled")) {
                                if (!pp.hasPermission("MCStatsNET.ban.bypass"))
                                    player.createCustomBan(new MCSConsole(), plugin.getConfig().getString("Modules.AntiVPN.auto-ban.reason"), plugin.getConfig().getInt("Modules.AntiVPN.auto-ban.expire"));
                            } else {
                                replace = new HashMap<>();
                                replace.put("id", checks.getVPN().getID());
                                player.disconnect(MCSCore.getInstance().buildScreen(lang, "checks.AntiVPN.screen", replace));
                            }
                            return;

                        } else {
                            HashMap<String, Object> replace = new HashMap<>();
                            replace.put("id", checks.getVPN().getID());
                            replace.put("playername", pp.getName());
                            MCSCore.getInstance().broadcast("MCStatsNET.AntiVPN.alert", "checks.prefix", "checks.AntiVPN.alertButIgnore", replace);
                        }
                    }
                }
            } catch (SQLException | InterruptedException | ExecutionException | IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    @EventHandler
    public void on(PlayerQuitEvent e) throws Exception {
        Player p = e.getPlayer();
        MCSCore.getInstance().playerQuit(p.getUniqueId());
    }

    @EventHandler
    public void on(AsyncPlayerChatEvent e) {
        //MCSCore.getInstance().getChatLog().log(e.getPlayer().getUniqueId(), ChatLogType.MESSAGE, e.getMessage());

        try {
            MCSCore.getInstance().getChatLog().log(new ChatLogDataMessage(e.getPlayer().getUniqueId(), e.getMessage()));

            MCSPlayer p = MCSCore.getInstance().getPlayer(e.getPlayer().getUniqueId());

            e.setCancelled(MCSCore.getInstance().getChatFilter().check(p, e.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void on(PlayerCommandPreprocessEvent e) {

        try {
            MCSCore.getInstance().getChatLog().log(new ChatLogDataCommand(e.getPlayer().getUniqueId(), e.getMessage()));

            MCSPlayer p = MCSCore.getInstance().getPlayer(e.getPlayer().getUniqueId());

            e.setCancelled(MCSCore.getInstance().getChatFilter().check(p, e.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public PluginDescription getDescription() {
        return new PluginDescription() {
            @Override
            public File getPlugin() {
                return plugin.getServer().getUpdateFolderFile();
            }

            @Override
            public String getName() {
                return plugin.getDescription().getName();
            }

            @Override
            public String getAuthor() {
                StringBuilder sb = new StringBuilder();

                for (String b : plugin.getDescription().getAuthors()) {
                    if (sb.length() != 0)
                        sb.append(", ");
                    sb.append(b);
                }

                return sb.toString();
            }

            @Override
            public String getVersion() {
                return plugin.getDescription().getVersion();
            }
        };
    }

    @Override
    public ServerDetails getServerDetails() {
        return new ServerDetails() {

            @Override
            public boolean isCloudSystem() {
                /*if (plugin.getServer().getPluginManager().isPluginEnabled("CloudNetAPI"))
                    return true;

                if (plugin.getServer().getPluginManager().isPluginEnabled("TimoCloudAPI"))
                    return true;*/

                if (isPluginEnabled("CloudNetAPI"))
                    return true;

                return isPluginEnabled("TimoCloudAPI");
            }

            @Override
            public CloudDetails getCloudSystem() {
                if (!isCloudSystem())
                    return null;

                return new CloudDetails() {

                    @Override
                    public String getId() {
                        if (getType().equals(CloudType.CLOUDNET))
                            return String.valueOf(CloudAPI.getInstance().getServiceId().getId());

                        if (getType().equals(CloudType.TIMOCLOUD))
                            return TimoCloudAPI.getBukkitAPI().getThisServer().getId();

                        return plugin.getServer().getServerId();
                    }

                    @Override
                    public String getWrapperId() {
                        if (getType().equals(CloudType.CLOUDNET))
                            return CloudAPI.getInstance().getServiceId().getWrapperId();

                        if (getType().equals(CloudType.TIMOCLOUD))
                            return TimoCloudAPI.getBukkitAPI().getThisServer().getBase();

                        return null;
                    }

                    @Override
                    public String getGroup() {
                        if (getType().equals(CloudType.CLOUDNET))
                            return CloudAPI.getInstance().getServiceId().getGroup();

                        if (getType().equals(CloudType.TIMOCLOUD))
                            return TimoCloudAPI.getBukkitAPI().getThisServer().getName();

                        if (plugin.getServer().getServerName().isEmpty())
                            return plugin.getServer().getName();

                        return plugin.getServer().getServerName();
                    }

                    @Override
                    public boolean isStatic() {
                        if (getType().equals(CloudType.CLOUDNET)) {
                            ServerGroupMode a = CloudAPI.getInstance().getServerGroup(CloudAPI.getInstance().getServiceId().getGroup()).getGroupMode();
                            return a.equals(ServerGroupMode.STATIC) || a.equals(ServerGroupMode.STATIC_LOBBY);
                        }

                        if (getType().equals(CloudType.TIMOCLOUD))
                            return TimoCloudAPI.getBukkitAPI().getThisServer().getGroup().isStatic();

                        return true;
                    }

                    @Override
                    public CloudType getType() {
                        if (isPluginEnabled("CloudNetAPI"))
                            return CloudType.CLOUDNET;

                        if (isPluginEnabled("TimoCloudAPI"))
                            return CloudType.TIMOCLOUD;

                        return null;
                    }
                };
            }

            @Override
            public String getName() {
                return plugin.getServer().getName();
            }

            @Override
            public int getPort() {
                return plugin.getServer().getPort();
            }

            @Override
            public boolean isOnlineMode() {
                return plugin.getServer().getOnlineMode();
            }

            @Override
            public ServerType getType() {
                return ServerType.BUKKIT;
            }

            @Override
            public String getVersion() {
                return plugin.getServer().getVersion();
            }
        };
    }

    @Override
    public void shutdown(String message) {
        Arrays.stream(getPlayers()).forEach(p -> p.disconnect(message));

        shutdown();
    }

    @Override
    public void shutdown() {
        plugin.getServer().shutdown();
    }

    @Override
    public String getDisplayName(MCSPlayer player) {
        if (isOnline(player)) {
            Player p = plugin.getServer().getPlayer(player.getUUID());
            assert p != null;

            if (p.getDisplayName().isEmpty())
                return p.getName();

            return p.getDisplayName();
        }

        return player.getName();
    }

    @Override
    public String getCustomName(MCSPlayer player) {
        if (isOnline(player)) {
            Player p = plugin.getServer().getPlayer(player.getUUID());
            assert p != null;

            if (p.getCustomName() == null || p.getCustomName().isEmpty())
                return p.getName();

            return p.getCustomName();
        }

        return player.getName();
    }

    @Override
    public MCSPlayer[] getPlayers() {
        return plugin.getServer().getOnlinePlayers().stream()
                .map((player -> {
                    try {
                        return MCSCore.getInstance().getPlayer(player.getUniqueId());
                    } catch (IOException | InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    return null;
                }))
                .collect(Collectors.toList()).toArray(new MCSPlayer[0]);
    }

    @Override
    public int getPing(MCSPlayer player) {
        if (!isOnline(player))
            return -1;

        Player p = plugin.getServer().getPlayer(player.getUUID());

        try {
            Reflection.FieldAccessor<Integer> ping = Reflection.getField(ep, "ping", int.class);
            return ping.get(getHandle.invoke(p));
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return -1;
    }

    @Override
    public boolean isOnline(MCSPlayer player) {
        return plugin.getServer().getPlayer(player.getUUID()) != null && plugin.getServer().getPlayer(player.getUUID()).isOnline();
    }

    @Override
    public void broadcast(String message) {
        for (MCSPlayer p : getPlayers())
            p.sendMessage(message);
    }

    @Override
    public void broadcast(String perm, String message) {
        for (MCSPlayer p : getPlayers())
            if (p.hasPermission(perm))
                p.sendMessage(message);
    }

    @Override
    public boolean hasPermission(MCSPlayer player, String s) {
        if (plugin.getServer().getPluginManager().isPluginEnabled("MCPerms"))
            if (MCPerms.getInstance().getManager().hasPermission(player, s).isAllowed())
                return true;

        if (!isOnline(player))
            return false;

        return plugin.getServer().getPlayer(player.getUUID()).hasPermission(s);
    }

    @Override
    public void disconnect(MCSPlayer player, String reason) {
        if (isOnline(player))
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getServer().getPlayer(player.getUUID()).kickPlayer(reason));
    }

    @Override
    public void playSound(MCSPlayer player, String sound, float volume, float pitch) {
        if (!isOnline(player))
            return;

        Sound sound1 = null;
        for (Sound a : Sound.values()) {
            System.out.println(a.name());
            if (a.name().equals(sound)) {
                sound1 = a;
                break;
            }
        }

        if (sound1 == null)
            return;

        Player p = plugin.getServer().getPlayer(player.getUUID());
        p.playSound(p.getLocation(), sound1, volume, pitch);
    }

    @Override
    public void sendTitle(MCSPlayer player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subTitle) {
        if (!isOnline(player))
            return;

        Player p = plugin.getServer().getPlayer(player.getUUID());
        TitleAPI.sendFullTitle(p, fadeIn, stay, fadeOut, title, subTitle);
    }

    @Override
    public void sendActionBar(MCSPlayer player, String message, int duration) {
        if (!isOnline(player))
            return;

        Player p = plugin.getServer().getPlayer(player.getUUID());
        new ActionBarAPI().sendActionBar(p, message, duration);
    }

    @Override
    public void sendMessage(MCSPlayer player, String message) {
        if (isOnline(player))
            plugin.getServer().getPlayer(player.getUUID()).sendMessage(message);
    }

    @Override
    public void sendMessage(MCSPlayer player, BaseComponent message) {
        if (isOnline(player))
            plugin.getServer().getPlayer(player.getUUID()).spigot().sendMessage(net.md_5.bungee.chat.ComponentSerializer.parse(ComponentSerializer.toString(message)));
    }

    @Override
    public void sendMessage(MCSPlayer player, BaseComponent[] message) {
        if (isOnline(player))
            plugin.getServer().getPlayer(player.getUUID()).spigot().sendMessage(net.md_5.bungee.chat.ComponentSerializer.parse(ComponentSerializer.toString(message)));
    }

    @Override
    public void sendConsole(String message) {
        plugin.getServer().getConsoleSender().sendMessage(message);
    }

    @Override
    public void sendConsole(BaseComponent message) {
        plugin.getServer().getConsoleSender().spigot().sendMessage(net.md_5.bungee.chat.ComponentSerializer.parse(ComponentSerializer.toString(message)));
    }

    @Override
    public void sendConsole(BaseComponent[] message) {
        plugin.getServer().getConsoleSender().spigot().sendMessage(net.md_5.bungee.chat.ComponentSerializer.parse(ComponentSerializer.toString(message)));
    }

    public boolean dispatchCommand(CommandSender cs, String cmd, String[] args) throws InterruptedException, ExecutionException, IOException {
        MCSEntity s;
        if (cs instanceof Player)
            s = MCSCore.getInstance().getPlayer(((Player)cs).getUniqueId());
        else
            s = new MCSConsole();

        return MCSCore.getInstance().dispatchCommand(s, cmd, args);
    }

    private static Property getSkin(String id) {
        try {
            RequestBuilder rb = new RequestBuilder("https://api.mcstats.net/v2/skin/" + id);

            RequestResponse rr = rb.get();

            if (rr.getStatusCode() != 200)
                return null;

            JsonObject data = rr.getContentJsonObject();
            JsonObject response = data.get("response").getAsJsonObject();
            JsonObject system = data.get("system").getAsJsonObject();

            if (system.get("status").getAsInt() != 200)
                return null;

            String value = response.get("value").getAsString();
            String signature = response.get("signature").getAsString();

            return new Property("textures", value, signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isPluginEnabled(String name) {
        for (Plugin plugin : plugin.getServer().getPluginManager().getPlugins()) {
            if (plugin.getDescription().getName().equalsIgnoreCase(name))
                return true;
        }

        return false;
    }
}
