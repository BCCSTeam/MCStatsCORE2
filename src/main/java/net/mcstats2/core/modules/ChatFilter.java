package net.mcstats2.core.modules;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.mcstats2.core.api.config.Configuration;
import net.mcstats2.core.utils.StringUtils;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFilter {
    private MCSCore core;

    private Pattern clean = Pattern.compile("\\w+", Pattern.UNICODE_CHARACTER_CLASS);
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

    public ChatFilter() {
        core = MCSCore.getInstance();
    }

    public boolean check(MCSPlayer p, String message) {
        boolean isCommand = message.startsWith("/");

        boolean check = true;
        if (isCommand) {
            check = false;

            for (String str : core.getConfig().getStringList("Modules.ChatFilter.commands")) {
                if (message.split(" ")[0].equalsIgnoreCase(str)) {
                    check = true;
                    break;
                }
            }
        }

        if (!check)
            return false;

        Configuration lang = p.getLang();

        try {
            if (p.getActiveMute() != null) {
                MCSPlayer.Mute mute = p.getActiveMute();

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

                    replace.put("expires", StringUtils.replace(p.getLang().getString("expires.temporary"), expires));
                } else
                    replace.put("expires", p.getLang().getString("expires.never"));

                p.sendMessage(MCSCore.getInstance().buildScreen(lang, "mute.screen", replace));
                return true;
            }


            // NOT UPDATED NO_SPAM
            /*if (plugin.getConfig().getBoolean("Modules.ChatFilter.spam.enabled")) {
                p.sendMessage(String.valueOf(plugin.players.get(p.getUUID().toString()).getAsJsonObject().get("chat").getAsJsonObject().get("last_message").getAsLong()));
                if (System.currentTimeMillis() - plugin.players.get(p.getUUID().toString()).getAsJsonObject().get("chat").getAsJsonObject().get("last_message").getAsLong() <= plugin.getConfig().getInt("Modules.ChatFilter.spam.delay")) {
                    pp.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', lang.getString("ChatFilter.prefix") + lang.getString("ChatFilter.spam"))));
                    e.setCancelled(true);
                    return;
                } else
                    plugin.players.get(p.getUUID().toString()).getAsJsonObject().get("chat").getAsJsonObject().addProperty("last_message", System.currentTimeMillis());
                p.sendMessage(String.valueOf(plugin.players.get(p.getUUID().toString()).getAsJsonObject().get("chat").getAsJsonObject().get("last_message").getAsLong()));
            }*/


            // Address Filter
            if (core.getConfig().getBoolean("Modules.ChatFilter.address.enabled")) {
                boolean block = false;
                Matcher m = urls.matcher(message);
                while (m.find()) {
                    block = true;

                    for (int i=0;i<m.groupCount();i++)
                        p.sendMessage(i + " - " + m.group(i));

                    for (String match : core.getConfig().getStringList("Modules.ChatFilter.address.whitelist")) {
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

                if (address.matcher(message).find() || block) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString("ChatFilter.prefix") + lang.getString("ChatFilter.address")));
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            p.sendMessage(lang.getString("ChatFilter.prefix") + "§r§cChat is disabled cause an Error");
            return true;
        }

        return false;
    }

    public int levenshteinDistance(CharSequence lhs, CharSequence rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        for(int i = 0; i < len0; i++) cost[i] = i;

        for(int j = 1; j < len1; j++) {
            newcost[0] = j;

            for(int i = 1; i < len0; i++) {
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                int cost_replace = cost[i - 1] + match;
                int cost_insert  = cost[i] + 1;
                int cost_delete  = newcost[i - 1] + 1;

                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            int[] swap = cost; cost = newcost; newcost = swap;
        }

        return cost[len0 - 1];
    }
}
