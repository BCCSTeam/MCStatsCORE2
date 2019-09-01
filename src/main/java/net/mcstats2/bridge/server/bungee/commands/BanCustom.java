package net.mcstats2.bridge.server.bungee.commands;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.MCSEntity.MCSConsole;
import net.mcstats2.core.api.MCSEntity.MCSEntity;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.mcstats2.core.api.config.Configuration;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class BanCustom extends Command {

    public BanCustom(String command) {
        super(command);
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        MCSEntity p = null;
        if (cs instanceof ProxiedPlayer) {
            try {
                p = MCSCore.getInstance().getPlayer(((ProxiedPlayer) cs).getUniqueId());
            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else
            p = new MCSConsole();

        if (p == null) {
            cs.sendMessage(TextComponent.fromLegacyText("§cError with your Profile!"));
            return;
        }

        Configuration lang = MCSCore.getInstance().getLang((p instanceof MCSPlayer) ? ((MCSPlayer) p).getSession().getAddressDetails().getLanguage() : "default");

        int m = cs instanceof ProxiedPlayer ? ((MCSPlayer) p).getMax("MCStatsNET.ban.power") : -1;

        if (m == 0 || !cs.hasPermission("MCStatsNET.ban.custom")) {
            cs.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', lang.getString("ban.prefix") + lang.getString("noPermissions"))));
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
                cs.sendMessage(TextComponent.fromLegacyText("§cError with the other Profile!"));
                return;
            }

            int tm = tp.getMax("MCStatsNET.ban.power");

            if (m != -1)
                if (m <= tm || tp.hasPermission("MCStatsNET.ban.bypass")) {
                    cs.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', lang.getString("ban.prefix") + lang.getString("ban.notAllowed").replace("%playername%", tp.getName()))));
                    return;
                }

            try {
                if (tp.getActiveBan() != null) {
                    cs.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', lang.getString("ban.prefix") + lang.getString("ban.alreadyBlocked").replace("%playername%", tp.getName()))));
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
                cs.sendMessage(TextComponent.fromLegacyText("§a/cban <player> <time|§e§nyMwdhms§r§a> <reason>"));
                return;
            }

            try {
                tp.createCustomBan(p, sb.toString(), expire);
            } catch (SQLException | InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        } else {
            cs.sendMessage(TextComponent.fromLegacyText("§a/cban <player> <time|yMwdhms> <reason>"));
        }
    }
}
