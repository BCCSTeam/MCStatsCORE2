package net.mcstats2.bridge.server.bungee.listeners;

import net.mcstats2.bridge.server.bungee.Core;
import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.mcstats2.core.api.config.Configuration;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFilter implements Listener {
    private Core plugin;

    private Pattern address = Pattern.compile("/([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})/g");
    private Pattern urls = Pattern.compile("\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/)?|www.?)" +
            "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov" +
            "|mil|biz|info|mobi|name|aero|jobs|museum" +
            "|travel|[a-z]{2}))(:[\\d]{1,5})?" +
            "(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?" +
            "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)" +
            "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*" +
            "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b");

    public ChatFilter(Core plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void on(ChatEvent e) throws SQLException {
        System.out.println(e.getMessage());

        if (!(e.getSender() instanceof ProxiedPlayer))
            return;

        boolean check = true;
        if (e.isCommand()) {
            check = false;

            for (String str : plugin.getConfig().getStringList("Modules.ChatFilter.commands")) {
                if (e.getMessage().split(" ")[0].equalsIgnoreCase(str)) {
                    check = true;
                    break;
                }
            }
        }

        if (!check)
            return;

        ProxiedPlayer pp = (ProxiedPlayer) e.getSender();

        MCSPlayer p = null;
        try {
            p = MCSCore.getInstance().getPlayer(pp.getUniqueId());
        } catch (IOException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
        if (p == null) {
            plugin.getConsole().sendMessage(TextComponent.fromLegacyText("§c" + pp.getName() + " error with MCS Profile."));
            return;
        }

        Configuration lang = MCSCore.getInstance().getLang(p.getSession().getAddressDetails().getLanguage());

        try {
            if (p.getActiveMute() != null) {
                MCSPlayer.Mute mute = p.getActiveMute();

                HashMap<String, Object> replace = new HashMap<>();
                replace.put("id", mute.getID());
                replace.put("reason", mute.getCustomReason() == null ? (mute.getReason() != null ? mute.getReason().getText() : "err") : (mute.getCustomReason().isEmpty() ? "&8&o<none>&r" : mute.getCustomReason()));

                if (mute.getExpire() != 0) {
                    long endsIn = Math.abs((System.currentTimeMillis() / 1000) - (mute.getExpire() + (mute.getTime() / 1000)));
                    long seconds = (endsIn) % 60;
                    long minutes = (endsIn / 60) % 60;
                    long hours = (endsIn / 60 / 60) % 24;
                    long days = (endsIn / 60 / 60 / 24);
                    replace.put("seconds", seconds);
                    replace.put("minutes", minutes);
                    replace.put("hours", hours);
                    replace.put("days", days);
                }

                p.sendMessage(MCSCore.getInstance().buildScreen(lang, mute.getExpire() != 0 ? "mute.temp.screen" : "mute.perm.screen", replace));
                e.setCancelled(true);
                return;
            }
        } catch (SQLException | InterruptedException | ExecutionException | IOException e1) {
            e1.printStackTrace();
        }

        if (plugin.getConfig().getBoolean("Modules.ChatFilter.spam.enabled")) {
            p.sendMessage(String.valueOf(plugin.players.get(p.getUUID().toString()).getAsJsonObject().get("chat").getAsJsonObject().get("last_message").getAsLong()));
            if (System.currentTimeMillis() - plugin.players.get(p.getUUID().toString()).getAsJsonObject().get("chat").getAsJsonObject().get("last_message").getAsLong() <= plugin.getConfig().getInt("Modules.ChatFilter.spam.delay")) {
                pp.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', lang.getString("chat.prefix") + lang.getString("chat.spam"))));
                e.setCancelled(true);
                return;
            } else
                plugin.players.get(p.getUUID().toString()).getAsJsonObject().get("chat").getAsJsonObject().addProperty("last_message", System.currentTimeMillis());
            p.sendMessage(String.valueOf(plugin.players.get(p.getUUID().toString()).getAsJsonObject().get("chat").getAsJsonObject().get("last_message").getAsLong()));
        }

        if (plugin.getConfig().getBoolean("Modules.ChatFilter.address.enabled")) {
            boolean block = false;
            Matcher m = urls.matcher(e.getMessage());
            while (m.find()) {
                block = true;

                for (int i=0;i<m.groupCount();i++)
                    p.sendMessage(i + " - " + m.group(i));

                for (String match : plugin.getConfig().getStringList("Modules.ChatFilter.address.whitelist")) {
                    match = match.replace(".", "\\.");
                    match = match.replace("*", "[a-zA-Z0-9]{1,}");

                    System.out.println("/" + match + "/i");

                    if (Pattern.compile("/" + match + "/i").matcher(m.group(6)).find()) {
                        block = false;
                        break;
                    }
                }

                if (!block)
                    break;
            }

            if (address.matcher(e.getMessage()).find() || block) {
                pp.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', lang.getString("chat.prefix") + lang.getString("chat.address"))));
                e.setCancelled(true);
                return;
            }
        }
    }
}
