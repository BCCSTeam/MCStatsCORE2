package net.mcstats2.core.api.commands;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.api.Command;
import net.mcstats2.core.api.MCSEntity.MCSEntity;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.mcstats2.core.api.config.Configuration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class BanCustom extends Command {

    public BanCustom(String command) {
        super(command);
    }

    @Override
    public void execute(MCSEntity p, String[] args) {
        Configuration lang = MCSCore.getInstance().getLang((p instanceof MCSPlayer) ? ((MCSPlayer) p).getSession().getAddressDetails().getLanguage() : "default");

        int m = p instanceof MCSPlayer ? ((MCSPlayer) p).getMax("MCStatsNET.ban.power") : -1;

        if (m == 0 || !p.hasPermission("MCStatsNET.ban.custom")) {
            p.sendMessage((ChatColor.translateAlternateColorCodes('&', lang.getString("ban.prefix") + lang.getString("noPermissions"))));
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

            int tm = tp.getMax("MCStatsNET.ban.power");

            if (m != -1)
                if (m <= tm || tp.hasPermission("MCStatsNET.ban.bypass")) {
                    p.sendMessage((ChatColor.translateAlternateColorCodes('&', lang.getString("ban.prefix") + lang.getString("ban.notAllowed").replace("%playername%", tp.getName()))));
                    return;
                }

            try {
                if (tp.getActiveBan() != null) {
                    p.sendMessage((ChatColor.translateAlternateColorCodes('&', lang.getString("ban.prefix") + lang.getString("ban.alreadyBlocked").replace("%playername%", tp.getName()))));
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

            int expire = MCSCore.getExpire(args[1]);

            if (expire == -1) {
                p.sendMessage(("§a/cban <player> <time|§e§nyMwdhms§r§a> <reason>"));
                return;
            }

            try {
                tp.createCustomBan(p, sb.toString(), expire);
            } catch (SQLException | InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        } else {
            p.sendMessage(("§a/cban <player> <time|yMwdhms> <reason>"));
        }
    }
}
