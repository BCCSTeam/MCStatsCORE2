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

public class Mute extends Command {

    public Mute(String command) {
        super(command);
    }

    @Override
    public void execute(MCSEntity p, String[] args) {

        Configuration lang = MCSCore.getInstance().getLang((p instanceof MCSPlayer) ? ((MCSPlayer) p).getSession().getAddressDetails().getLanguage() : "default");

        int m = p instanceof MCSPlayer ? ((MCSPlayer) p).getMax("MCStatsNET.mute.power") : -1;
        int templatePower = p instanceof MCSPlayer ? ((MCSPlayer) p).getMax("MCStatsNET.mute.template.power") : -1;

        if (templatePower == -1)
            templatePower = 255;

        if (m == 0 || templatePower == 0) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString("mute.prefix") + lang.getString("noPermissions")));
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
                p.sendMessage("§cError with the other Profile!");
                return;
            }

            int tm = tp.getMax("MCStatsNET.mute.power");

            if (m != -1)
                if (m <= tm || tp.hasPermission("MCStatsNET.mute.bypass")) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString("mute.prefix") + lang.getString("mute.notAllowed").replace("%playername%", tp.getName() )));
                    return;
                }

            try {
                if (tp.getActiveMute() != null) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString("mute.prefix") + lang.getString("mute.alreadyBlocked").replace("%playername%", tp.getName())));
                    return;
                }
            } catch (SQLException | InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }

            MCSCore.MuteTemplate bt = null;
            try {
                bt = MCSCore.getInstance().getMuteTemplateByName(args[1]);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (bt == null) {
                try {
                    p.sendMessage();
                    for (MCSCore.MuteTemplate bt1 : MCSCore.getInstance().getMuteTemplates(args[1], templatePower)) {
                        String name = "§6" + bt1.getName().replace(args[1],"§e" + args[1] + "§r§6");

                        StringBuilder expires = new StringBuilder();
                        for (int i : bt1.getExpires()) {
                            if (expires.length() > 0)
                                expires.append(", ");

                            if (i == 0) {
                                expires.append(lang.getString("permanent"));
                                continue;
                            }

                            long endsIn = Math.abs(i);
                            long seconds = (endsIn) % 60;
                            long minutes = (endsIn / 60) % 60;
                            long hours = (endsIn / 60 / 60) % 24;
                            long days = (endsIn / 60 / 60 / 24);

                            if (days != 0)
                                expires.append(days).append("d");
                            if (hours != 0)
                                expires.append(hours).append("h");
                            if (minutes != 0)
                                expires.append(minutes).append("m");
                            if (seconds != 0)
                                expires.append(seconds).append("s");
                        }

                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString("mute.prefix") + name + " §8- §e" + bt1.getText() + " §8[§7" + expires + "§8]"));
                    }
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString("mute.unknowntemplate")));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return;
            }

            try {
                tp.createMute(p, bt);
            } catch (SQLException | InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                p.sendMessage();
                for (MCSCore.MuteTemplate bt1 : MCSCore.getInstance().getMuteTemplates(templatePower)) {

                    StringBuilder expires = new StringBuilder();
                    for (int i : bt1.getExpires()) {
                        if (expires.length() > 0)
                            expires.append(", ");

                        if (i == 0) {
                            expires.append(lang.getString("permanent"));
                            continue;
                        }

                        long endsIn = Math.abs(i);
                        long seconds = (endsIn) % 60;
                        long minutes = (endsIn / 60) % 60;
                        long hours = (endsIn / 60 / 60) % 24;
                        long days = (endsIn / 60 / 60 / 24);

                        if (days != 0)
                            expires.append(days).append("d");
                        if (hours != 0)
                            expires.append(hours).append("h");
                        if (minutes != 0)
                            expires.append(minutes).append("m");
                        if (seconds != 0)
                            expires.append(seconds).append("s");
                    }

                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString("mute.prefix") + bt1.getName() + " §8- §e" + bt1.getText() + " §8[§7" + expires + "§8]"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            p.sendMessage("§a/mute <player> <reason>");
        }
    }
}
