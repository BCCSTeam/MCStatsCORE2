package net.mcstats2.core.commands;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.api.Command;
import net.mcstats2.core.api.MCSEntity.MCSEntity;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.mcstats2.core.api.config.Configuration;
import net.mcstats2.core.utils.StringUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class MuteCustom extends Command {

    public MuteCustom(String command) {
        super(command);
    }

    @Override
    public void execute(MCSEntity p, String[] args) {
        Configuration lang = MCSCore.getInstance().getLang((p instanceof MCSPlayer) ? ((MCSPlayer) p).getSession().getAddressDetails().getLanguage() : "default");

        int m = p instanceof MCSPlayer ? ((MCSPlayer) p).getMax("MCStatsNET.mute.power") : -1;

        if (m == 0 || !p.hasPermission("MCStatsNET.mute.custom")) {
            p.sendMessage((ChatColor.translateAlternateColorCodes('&', lang.getString("mute.prefix") + lang.getString("noPermissions"))));
            return;
        }

        if (args.length >= 2) {
            MCSPlayer tp = null;
            try {
                tp = MCSCore.getInstance().getPlayer(args[0]);
            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            if (tp == null) {
                p.sendMessage(("§cError with the other Profile!"));
                return;
            }

            if (p.equals(tp)) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString("ban.self")));
                return;
            }

            int tm = tp.getMax("MCStatsNET.mute.power");

            if (m != -1)
                if (m <= tm || tp.hasPermission("MCStatsNET.mute.bypass")) {
                    p.sendMessage((ChatColor.translateAlternateColorCodes('&', lang.getString("mute.prefix") + lang.getString("mute.notAllowed").replace("%playername%", tp.getName()))));
                    return;
                }

            try {
                if (tp.getActiveMute() != null) {
                    p.sendMessage((ChatColor.translateAlternateColorCodes('&', lang.getString("mute.prefix") + lang.getString("mute.alreadyBlocked").replace("%playername%", tp.getName()))));
                    return;
                }
            } catch (SQLException | InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }

            StringBuilder sb = new StringBuilder();
            for (int i=2;i<args.length;i++) {
                if (sb.length() != 0)
                    sb.append(" ");
                sb.append(args[i]);
            }

            int expire = StringUtils.getExpire(args[1]);

            if (expire == -1) {
                p.sendMessage(("§a/cmute <player> <time|§e§nyMwdhms§r§a> <reason>"));
                return;
            }

            try {
                tp.createCustomMute(p, sb.toString(), expire);
            } catch (SQLException | InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        } else {
            p.sendMessage(("§a/cmute <player> <time|yMwdhms> <reason>"));
        }
    }
}
