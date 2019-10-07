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

public class Ban extends Command {

    public Ban(String command) {
        super(command);
    }

    @Override
    public void execute(MCSEntity p, String[] args) {
        Configuration lang = MCSCore.getInstance().getLang((p instanceof MCSPlayer) ? ((MCSPlayer) p).getSession().getAddressDetails().getLanguage() : "default");

        int m = p instanceof MCSPlayer ? ((MCSPlayer) p).getMax("MCStatsNET.ban.power") : -1;
        int templatePower = p instanceof MCSPlayer ? ((MCSPlayer) p).getMax("MCStatsNET.ban.template.power") : -1;

        if (templatePower == -1)
            templatePower = 255;

        if (m == 0 || templatePower == 0) {
            p.sendMessage((ChatColor.translateAlternateColorCodes('&', lang.getString("ban.prefix") + lang.getString("noPermissions"))));
            return;
        }

        if (args.length == 2) {
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

            MCSCore.BanTemplate bt = null;
            try {
                bt = MCSCore.getInstance().getBanTemplateByName(args[1]);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (bt == null) {
                try {
                    p.sendMessage();
                    for (MCSCore.BanTemplate bt1 : MCSCore.getInstance().getBanTemplates(args[1], templatePower)) {
                        String name = "§6" + bt1.getName().replace(args[1],"§e" + args[1] + "§r§6");

                        String expires = "";
                        for (int i : bt1.getExpires()) {
                            if (!expires.isEmpty())
                                expires += ", ";

                            if (i == 0) {
                                expires += lang.getString("permanent");
                                continue;
                            }

                            long endsIn = Math.abs(i);
                            long seconds = (endsIn) % 60;
                            long minutes = (endsIn / 60) % 60;
                            long hours = (endsIn / 60 / 60) % 24;
                            long days = (endsIn / 60 / 60 / 24);

                            if (days != 0)
                                expires += days + "d";
                            if (hours != 0)
                                expires += hours + "h";
                            if (minutes != 0)
                                expires += minutes + "m";
                            if (seconds != 0)
                                expires += seconds + "s";
                        }

                        p.sendMessage((ChatColor.translateAlternateColorCodes('&', lang.getString("ban.prefix") + name + " §8- §e" + bt1.getText() + " §8[§7" + expires + "§8]")));
                    }
                    p.sendMessage((ChatColor.translateAlternateColorCodes('&', lang.getString("ban.unknowntemplate"))));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return;
            }

            try {
                tp.createBan(p, bt);
            } catch (SQLException | InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                p.sendMessage();
                for (MCSCore.BanTemplate bt1 : MCSCore.getInstance().getBanTemplates(templatePower)) {

                    String expires = "";
                    for (int i : bt1.getExpires()) {
                        if (!expires.isEmpty())
                            expires += ", ";

                        if (i == 0) {
                            expires += lang.getString("permanent");
                            continue;
                        }

                        long endsIn = Math.abs(i);
                        long seconds = (endsIn) % 60;
                        long minutes = (endsIn / 60) % 60;
                        long hours = (endsIn / 60 / 60) % 24;
                        long days = (endsIn / 60 / 60 / 24);

                        if (days != 0)
                            expires += days + "d";
                        if (hours != 0)
                            expires += hours + "h";
                        if (minutes != 0)
                            expires += minutes + "m";
                        if (seconds != 0)
                            expires += seconds + "s";
                    }

                    p.sendMessage((ChatColor.translateAlternateColorCodes('&', lang.getString("ban.prefix") + bt1.getName() + " §8- §e" + bt1.getText() + " §8[§7" + expires + "§8]")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            p.sendMessage(("§a/ban <player> <reason>"));
        }
    }
}
