package net.mcstats2.bridge.server.bungee.listeners;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.mcstats2.bridge.server.bungee.Core;
import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSEntity.MCSConsole;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.mcstats2.core.api.config.Configuration;
import net.mcstats2.core.network.web.data.MCSPlayerData;
import net.mcstats2.core.utils.StringUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class PlayerJoin implements Listener {
    private Core plugin;

    public PlayerJoin(Core plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void on(LoginEvent e) {
        try {
            PendingConnection pp = e.getConnection();

            System.out.println(pp.getName() + "[" + pp.getUniqueId().toString() + "] - Fetching MCSProfile...");

            MCSPlayer player = null;
            try {
                player = MCSCore.getInstance().playerJoin(pp.getUniqueId(), pp.getName(), pp.getAddress().getAddress().getHostAddress(), pp.getVirtualHost().getHostString(), pp.getVersion());
            } catch (IOException | InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }

            if (player == null) {
                pp.disconnect(TextComponent.fromLegacyText("§cThere was an Error with your profile!"));
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
                    HashMap<String, Object> expires = new HashMap<>();
                    long endsIn = Math.abs((System.currentTimeMillis() / 1000) - (ban.getExpire() + (ban.getTime() / 1000)));
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

                    replace.put("expires", StringUtils.replace(player.getLang().getString("expires.temporary"), expires));
                } else
                    replace.put("expires", player.getLang().getString("expires.never"));

                player.sendMessage(MCSCore.getInstance().buildScreen(lang, "ban.screen", replace));
                return;
            }

            if (session.getChecks() != null) {
                MCSPlayer.Session.Checks checks = session.getChecks();

                if (plugin.getConfig().getBoolean("Modules.gBan.enabled") && checks.getGBan() != null) {
                    if (!player.hasPermission("MCStatsNET.gban.bypass")) {
                        plugin.getConsole().sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', checks.getGBan().getAlert())));
                        for (ProxiedPlayer pp1 : plugin.getProxy().getPlayers())
                            if (pp1.hasPermission("MCStatsNET.gban.alert"))
                                pp1.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', checks.getGBan().getAlert())));

                        pp.disconnect(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', checks.getGBan().getScreen())));

                        return;
                    }
                }

                if (plugin.getConfig().getBoolean("Modules.SkinChecker.enabled") && player.getSkin() != null && player.getSkin().getStatus().equals(MCSPlayerData.SkinStatus.BLACKLISTED)) {
                    if (!player.hasPermission("MCStatsNET.SkinChecker.bypass")) {
                        HashMap<String, Object> replace = new HashMap<>();
                        replace.put("id", player.getSkin().getID());
                        replace.put("playername", pp.getName());
                        MCSCore.getInstance().broadcast("MCStatsNET.SkinChecker.alert", "checks.prefix", "checks.SkinChecker.alert", replace);

                        if (plugin.getConfig().getBoolean("Modules.SkinChecker.auto-ban.enabled")) {
                            if (!player.hasPermission("MCStatsNET.ban.bypass"))
                                player.createCustomBan(new MCSConsole(), plugin.getConfig().getString("Modules.SkinChecker.auto-ban.reason"), plugin.getConfig().getInt("Modules.SkinChecker.auto-ban.expire"));
                        } else {
                            replace = new HashMap<>();
                            replace.put("id", player.getSkin().getID());
                            replace.put("playername", pp.getName());
                            pp.disconnect(MCSCore.getInstance().buildScreen(lang, "checks.SkinChecker.screen", replace));
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
                    if (!player.hasPermission("MCStatsNET.AntiVPN.bypass")) {
                        HashMap<String, Object> replace = new HashMap<>();
                        replace.put("id", checks.getVPN().getID());
                        replace.put("playername", pp.getName());
                        MCSCore.getInstance().broadcast("MCStatsNET.AntiVPN.alert", "checks.prefix", "checks.AntiVPN.alert", replace);

                        if (plugin.getConfig().getBoolean("Modules.AntiVPN.auto-ban.enabled")) {
                            if (!player.hasPermission("MCStatsNET.ban.bypass"))
                                player.createCustomBan(new MCSConsole(), plugin.getConfig().getString("Modules.AntiVPN.auto-ban.reason"), plugin.getConfig().getInt("Modules.AntiVPN.auto-ban.expire"));
                        } else {
                            replace = new HashMap<>();
                            replace.put("id", checks.getVPN().getID());
                            pp.disconnect(MCSCore.getInstance().buildScreen(lang, "checks.AntiVPN.screen", replace));
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

            JsonObject data = new JsonObject();
            JsonObject chat = new JsonObject();
            chat.addProperty("last_message", 0);
            chat.add("history", new JsonArray());
            data.add("chat", chat);
            plugin.players.add(player.getUUID().toString(), data);
        } catch (SQLException | InterruptedException | ExecutionException | IOException ex) {
            ex.printStackTrace();
        }
    }
}
