package net.mcstats2.core.api.MCSServer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.api.MCSEntity.MCSConsole;
import net.mcstats2.core.api.MCSEntity.MCSEntity;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.mcstats2.core.api.config.Configuration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MCSBukkitServer implements MCSServer, Listener {
    private Plugin plugin;

    public MCSBukkitServer(Plugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void on(PlayerLoginEvent e) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Player pp = e.getPlayer();

                System.out.println(pp.getName() + "[" + pp.getUniqueId().toString() + "] - Fetching MCSProfile...");

                MCSPlayer player = null;
                try {
                    player = MCSCore.getInstance().playerJoin(pp.getUniqueId(),
                            pp.getName(),
                            e.getAddress().getHostAddress(),
                            e.getHostname(),
                            -1);
                } catch (IOException | InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                }

                if (player == null) {
                    pp.kickPlayer(("§cThere was an Error with your profile!"));
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

                    pp.kickPlayer(MCSCore.getInstance().buildScreen(lang, ban.getExpire() != 0 ? "ban.temp.screen" : "ban.perm.screen", replace));
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

                            pp.kickPlayer((ChatColor.translateAlternateColorCodes('&', checks.getGBan().getScreen())));

                            return;
                        }
                    }

                    if (plugin.getConfig().getBoolean("Modules.SkinChecker.enabled") && checks.getSkin() != null && checks.getSkin().isBlocked()) {
                        if (!pp.hasPermission("MCStatsNET.SkinChecker.bypass")) {
                            HashMap<String, Object> replace = new HashMap<>();
                            replace.put("id", checks.getVPN().getID());
                            replace.put("playername", pp.getName());
                            MCSCore.getInstance().broadcast("MCStatsNET.SkinChecker.alert", "checks.prefix", "checks.SkinChecker.alert", replace);

                            if (plugin.getConfig().getBoolean("Modules.SkinChecker.auto-ban.enabled")) {
                                if (!pp.hasPermission("MCStatsNET.ban.bypass"))
                                    player.createCustomBan(new MCSConsole(), plugin.getConfig().getString("Modules.SkinChecker.auto-ban.reason"), plugin.getConfig().getInt("Modules.SkinChecker.auto-ban.expire"));
                            } else {
                                replace = new HashMap<>();
                                replace.put("id", checks.getVPN().getID());
                                replace.put("playername", pp.getName());
                                pp.kickPlayer(MCSCore.getInstance().buildScreen(lang, "checks.SkinChecker.screen", replace));
                            }
                            return;

                        } else {
                            HashMap<String, Object> replace = new HashMap<>();
                            replace.put("id", checks.getSkin().getID());
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
                                pp.kickPlayer(MCSCore.getInstance().buildScreen(lang, "checks.AntiVPN.screen", replace));
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

    @Override
    public PluginDescription getDescription() {
        return new PluginDescription() {
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
    public MCSPlayer[] getPlayers() throws InterruptedException, ExecutionException, IOException {
        List<MCSPlayer> data = new ArrayList<>();

        for(Player p : plugin.getServer().getOnlinePlayers())
            data.add(MCSCore.getInstance().getPlayer(p.getUniqueId()));

        return data.toArray(new MCSPlayer[0]);
    }

    @Override
    public boolean isOnline(MCSPlayer player) {
        return plugin.getServer().getPlayer(player.getUUID()) != null && plugin.getServer().getPlayer(player.getUUID()).isOnline();
    }

    @Override
    public void broadcast(String perm, String message) {
        plugin.getServer().broadcast(perm, message);
    }

    @Override
    public boolean hasPermission(MCSPlayer player, String s) {
        return plugin.getServer().getPlayer(player.getUUID()).hasPermission(s);
    }

    @Override
    public void disconnect(MCSPlayer player, String reason) {
        if (plugin.getServer().getPlayer(player.getUUID()).isOnline())
            plugin.getServer().getPlayer(player.getUUID()).kickPlayer(reason);
    }

    @Override
    public void sendMessage(MCSPlayer player, String message) {
        if (plugin.getServer().getPlayer(player.getUUID()).isOnline())
            plugin.getServer().getPlayer(player.getUUID()).sendMessage(message);
    }

    @Override
    public void sendConsole(String message) {
        plugin.getServer().getConsoleSender().sendMessage(message);
    }

    public boolean dispatchCommand(CommandSender cs, String cmd, String[] args) throws InterruptedException, ExecutionException, IOException {
        MCSEntity s;
        if (cs instanceof Player)
            s = MCSCore.getInstance().getPlayer(((Player)cs).getUniqueId());
        else
            s = new MCSConsole();

        return MCSCore.getInstance().dispatchCommand(s, cmd, args);
    }
}
