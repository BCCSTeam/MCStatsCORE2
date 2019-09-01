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
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class BanRemove extends Command {

    public BanRemove(String command) {
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

        int m = cs instanceof ProxiedPlayer ? ((MCSPlayer) p).getMax("MCStatsNET.unban.power") : -1;
        int templatePower = cs instanceof ProxiedPlayer ? ((MCSPlayer) p).getMax("MCStatsNET.unban.template.power") : -1;

        if (m == 0 || templatePower == 0) {
            cs.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', lang.getString("unban.prefix") + lang.getString("noPermissions"))));
            return;
        }

        if (args.length == 1) {
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

            MCSPlayer.Ban ban = null;
            try {
                ban = tp.getActiveBan();
            } catch (SQLException | InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }

            if (ban == null || !ban.isValid()) {
                cs.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', lang.getString("unban.prefix") + lang.getString("unban.notBanned").replace("%playername%", tp.getName()))));
                return;
            }

            if (ban.getSTAFF() instanceof MCSPlayer && m != -1) {
                MCSPlayer s = (MCSPlayer)ban.getSTAFF();
                if (m < s.getMax("MCStatsNET.ban.power")) {
                    cs.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', lang.getString("unban.prefix") + lang.getString("noPermissions"))));
                    return;
                }
            }

            if (ban.getReason() != null && templatePower != -1) {
                MCSCore.BanTemplate bt = ban.getReason();

                if (templatePower < bt.getPower()) {
                    cs.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', lang.getString("unban.prefix") + lang.getString("noPermissions"))));
                    return;
                }
            }

            if (ban.delete()) {
                HashMap<String, Object> replace = new HashMap<>();
                replace.put("playername", tp.getName());
                replace.put("staffname", p.getName());
                try {
                    MCSCore.getInstance().broadcast("MCStatsNET.ban.alert", "unban.prefix", "unban.alert", replace);
                } catch (InterruptedException | ExecutionException | IOException e) {
                    e.printStackTrace();
                }
            } else
                cs.sendMessage(TextComponent.fromLegacyText("§cFailed!"));
        } else
            cs.sendMessage(TextComponent.fromLegacyText("§a/unban <player>"));
    }
}
